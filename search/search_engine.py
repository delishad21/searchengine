import sqlite3
import math
from utils import process_text, extract_phrases_and_terms, load_stopwords, tokenize, ps

stopwords = load_stopwords()

def search(query, db_path="../search_index.db", max_results=50):
    phrases, terms = extract_phrases_and_terms(query)
    conn = sqlite3.connect(db_path)
    conn.row_factory = sqlite3.Row
    cursor = conn.cursor()

    # Step 1: Get term frequencies
    term_docs = {}  # word -> list of (page_id, freq)
    doc_freq = {}   # word -> df

    for term in terms:
        cursor.execute("SELECT page_id, frequency FROM keywords WHERE word = ?", (term,))
        results = cursor.fetchall()
        term_docs[term] = results
        doc_freq[term] = len(set(r["page_id"] for r in results))

    # Step 2: Build document vectors
    doc_vectors = {}
    doc_max_tf = {}

    for term, postings in term_docs.items():
        for row in postings:
            pid, freq = row["page_id"], row["frequency"]
            doc_vectors.setdefault(pid, {})[term] = freq
            doc_max_tf[pid] = max(doc_max_tf.get(pid, 0), freq)

    N = cursor.execute("SELECT COUNT(*) FROM pages").fetchone()[0]

    scores = {}
    for pid, vector in doc_vectors.items():
        doc_vec = []
        query_vec = []
        max_tf = doc_max_tf[pid]

        for term in terms:
            tf = vector.get(term, 0)
            tfidf = (tf / max_tf) * math.log((N + 1) / (doc_freq.get(term, 1) + 1))
            doc_vec.append(tfidf)
            query_vec.append(1)

        # Cosine similarity
        dot_product = sum(a * b for a, b in zip(doc_vec, query_vec))
        norm_doc = math.sqrt(sum(a * a for a in doc_vec))
        norm_query = math.sqrt(len(query_vec))

        if norm_doc == 0 or norm_query == 0:
            continue

        score = dot_product / (norm_doc * norm_query)

        # Phrase boost
        cursor.execute("SELECT title, metadata FROM pages WHERE id = ?", (pid,))
        title_row = cursor.fetchone()
        title = title_row["title"]

        # Title match boost
        if any(term in title for term in terms):
            score *= 1.5

        # Phrase match boost
        cursor.execute("SELECT url FROM urls WHERE page_id = ?", (pid,))
        url = cursor.fetchone()["url"]

        cursor.execute("SELECT GROUP_CONCAT(word, ' ') as full_text FROM keywords WHERE page_id = ?", (pid,))
        text = cursor.fetchone()["full_text"] or ""

        for phrase in phrases:
            if phrase in text.lower():
                score *= 2

        scores[pid] = score

    # Step 3: Get top results
    top_pages = sorted(scores.items(), key=lambda x: -x[1])[:max_results]
    results = []

    for pid, score in top_pages:
        cursor.execute("SELECT title, metadata FROM pages WHERE id = ?", (pid,))
        page = cursor.fetchone()
        title = page["title"]
        metadata = page["metadata"]

        cursor.execute("SELECT url FROM urls WHERE page_id = ?", (pid,))
        url = cursor.fetchone()["url"]

        cursor.execute("SELECT word, frequency FROM keywords WHERE page_id = ? ORDER BY frequency DESC LIMIT 5", (pid,))
        keywords = cursor.fetchall()

        cursor.execute("SELECT parent_id FROM links WHERE child_id = ?", (pid,))
        parents = [row["parent_id"] for row in cursor.fetchall()]

        cursor.execute("SELECT child_id FROM links WHERE parent_id = ?", (pid,))
        children = [row["child_id"] for row in cursor.fetchall()]

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
