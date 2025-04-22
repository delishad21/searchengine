import sqlite3
import math
from utils import extract_phrases_and_terms, load_stopwords

stopwords = load_stopwords()

def search(query, db_path="../search_index.db", max_results=50): # max 50 results as defined for final submission
    phrases, terms = extract_phrases_and_terms(query)
    conn = sqlite3.connect(db_path)
    conn.row_factory = sqlite3.Row
    cursor = conn.cursor()

    phrase_docs = {}
    phrase_doc_freq = {}

    for phrase in phrases:
        if len(phrase.split()) == 2:
            cursor.execute("SELECT page_id, frequency FROM bigrams WHERE bigram = ?", (phrase,))
            results = cursor.fetchall()
            phrase_docs[phrase] = results
            print(results)
            phrase_doc_freq[phrase] = len(set(r["page_id"] for r in results))
        elif len(phrase.split()) == 3:
            cursor.execute("SELECT page_id, frequency FROM trigrams WHERE trigram = ?", (phrase,))
            results = cursor.fetchall()
            phrase_docs[phrase] = results
            phrase_doc_freq[phrase] = len(set(r["page_id"] for r in results))
    
    phrase_doc_vectors = {}
    phrase_doc_tf = {}
    phrase_max_tf = {}

    for phrase, postings in phrase_docs.items():
        for row in postings:
            pid, freq = row["page_id"], row["frequency"]
            if len(phrase.split()) == 2:
                cursor.execute("SELECT COUNT(page_id) FROM bigrams WHERE page_id = ?", (pid,))
            elif len(phrase.split()) == 3:
                cursor.execute("SELECT COUNT(page_id) FROM trigrams WHERE page_id = ?", (pid,))
            total_terms = cursor.fetchone()[0]

            phrase_doc_tf[pid] = total_terms
            phrase_doc_vectors.setdefault(pid, {})[phrase] = freq
    
    for pid in phrase_doc_vectors.keys():
        cursor.execute("""
            SELECT MAX(max_freq) as max_freq FROM (
            SELECT MAX(frequency) as max_freq 
            FROM bigrams 
            WHERE page_id = ?
            UNION ALL
            SELECT MAX(frequency) as max_freq 
            FROM trigrams 
            WHERE page_id = ?
            )
        """, (pid, pid))
        result = cursor.fetchone()
        phrase_max_tf[pid] = result["max_freq"] if result["max_freq"] is not None else 0

    # Retrieve total number of pages crawled
    N = cursor.execute("SELECT COUNT(*) FROM pages").fetchone()[0]
    
    scores = {}

    for pid, vector in phrase_doc_vectors.items():
        phrase_doc_vec = []
        phrase_query_vec = []


        for phrase in phrases:
            phrase_tf = vector.get(phrase, 0)
            phrase_tfidf = phrase_tf / phrase_max_tf[pid] * math.log((N + 1) / (phrase_doc_freq.get(phrase, 1) + 1))
            phrase_doc_vec.append(phrase_tfidf)
            phrase_query_vec.append(1)
        if len(phrases) == 1:
            # Single-term query: rank by raw TF-IDF
            score = phrase_tfidf 
            
        else:
            dot_product = sum(a * b for a, b in zip(phrase_doc_vec, phrase_query_vec))
            norm_doc = math.sqrt(sum(a * a for a in phrase_doc_vec))
            norm_query = float(math.sqrt(len(phrase_query_vec)))
            if norm_doc == 0 or norm_query == 0:
                score = 0
            else:
                score = dot_product / (norm_doc * norm_query)
        
        scores[pid] = score


    # For each term in the query:
    # Fetch all pages where the term appears (from the keywords table).
    # Store:
    # Docs that the term appears in (term_docs)
    # How many docs it appears in (doc_freq)

    term_docs = {}
    doc_freq = {}

    for term in terms:
        cursor.execute("SELECT page_id, frequency FROM keywords WHERE word = ?", (term,)) # keywords is inverted index
        results = cursor.fetchall()
        term_docs[term] = results
        doc_freq[term] = len(set(r["page_id"] for r in results))

    # Build TF Vectors for Documents
    doc_vectors = {}
    doc_tf = {}
    max_tf = {}

    for term, postings in term_docs.items():
        for row in postings:
            pid, freq = row["page_id"], row["frequency"]
            cursor.execute("SELECT COUNT(page_id) FROM keywords WHERE page_id = ?", (pid,))
            total_terms = cursor.fetchone()[0]

            doc_tf[pid] = total_terms
            doc_vectors.setdefault(pid, {})[term] = freq

    # Calculate the true max_tf for all documents we've seen
    max_tf = {}
    for pid in doc_vectors.keys():
        cursor.execute("""
            SELECT MAX(frequency) as max_freq 
            FROM keywords 
            WHERE page_id = ?
        """, (pid,))
        result = cursor.fetchone()
        max_tf[pid] = result["max_freq"] if result["max_freq"] is not None else 0

    # Calculate TF-IDF for each document (with boost from title match and phrase match)
    for pid, vector in doc_vectors.items():
        doc_vec = []
        query_vec = []

        for term in terms:
            tf = vector.get(term, 0)
            tfidf = tf / max_tf[pid] * math.log((N + 1) / (doc_freq.get(term, 1) + 1))
            doc_vec.append(tfidf)
            query_vec.append(1)
        if len(terms) == 1:
            # Single-term query: rank by raw TF-IDF
            score = tfidf 
            
        else:
            dot_product = sum(a * b for a, b in zip(doc_vec, query_vec))
            norm_doc = math.sqrt(sum(a * a for a in doc_vec))
            norm_query = float(math.sqrt(len(query_vec)))
            if norm_doc == 0 or norm_query == 0:
                score = 0
            else:
                score = dot_product / (norm_doc * norm_query)

    

        # Title match boost (use stemmed_title)
        cursor.execute("SELECT stemmed_title FROM pages WHERE id = ?", (pid,))
        title_row = cursor.fetchone()
        stemmed_title = title_row["stemmed_title"]

        if any(term in stemmed_title for term in terms):
            score *= 1.5

        scores[pid] = score if pid not in scores else scores[pid] + score

    top_pages = sorted(scores.items(), key=lambda x: -x[1])[:max_results]
    results = []

    for pid, score in top_pages:
        cursor.execute("SELECT original_title, metadata FROM pages WHERE id = ?", (pid,))
        page = cursor.fetchone()
        title = page["original_title"]
        metadata = page["metadata"]

        cursor.execute("SELECT url FROM urls WHERE page_id = ?", (pid,))
        url = cursor.fetchone()["url"]

        cursor.execute("SELECT word, frequency FROM keywords WHERE page_id = ? ORDER BY frequency DESC LIMIT 5", (pid,))
        keywords = cursor.fetchall()

        # Get parent links as URLs
        cursor.execute("""
            SELECT u.url FROM links l
            JOIN urls u ON l.parent_id = u.page_id
            WHERE l.child_id = ?
        """, (pid,))
        parents = [row["url"] for row in cursor.fetchall()]

        # Get child links as URLs
        cursor.execute("""
            SELECT u.url FROM links l
            JOIN urls u ON l.child_id = u.page_id
            WHERE l.parent_id = ?
        """, (pid,))
        children = [row["url"] for row in cursor.fetchall()]

        results.append({
            "score": round(score, 4),
            "title": title,
            "url": url,
            "metadata": metadata,
            "keywords": [{"word": row["word"], "freq": row["frequency"]} for row in keywords],
            "parents": parents,
            "children": children
        })

    conn.close()
    return results
