import re
import math
import sqlite3
from nltk.stem import PorterStemmer

ps = PorterStemmer()

def load_stopwords(filepath="stopwords.txt"):
    with open(filepath, "r") as f:
        return set(line.strip() for line in f)

def tokenize(text):
    return re.findall(r'\w+', text.lower())

def process_text(text, stopwords):
    return [ps.stem(word) for word in tokenize(text) if word not in stopwords]

def extract_phrases_and_terms(query):
    import shlex
    try:
        tokens = shlex.split(query)
    except ValueError as e:
        if "No closing quotation" in str(e):
            tokens = []
            current_token = []
            in_quote = False
            
            for char in query:
                if char in ('"', "'"):
                    if in_quote:
                        # Close the quoted phrase
                        tokens.append(''.join(current_token))
                        current_token = []
                        in_quote = False
                    else:
                        # Start new quoted phrase
                        if current_token:  # Add any preceding unquoted text
                            tokens.extend(''.join(current_token).split())
                            current_token = []
                        in_quote = True
                elif char.isspace() and not in_quote:
                    if current_token:  # Add completed unquoted token
                        tokens.append(''.join(current_token))
                        current_token = []
                else:
                    current_token.append(char)
            
            # Add any remaining tokens
            if current_token:
                tokens.append(''.join(current_token))
        else:
            raise e
    
    # Process tokens into phrases and terms
    print(tokens)
    phrases = [ps.stem(word) 
               for t in tokens 
               if (" " in t) or ("'" in t) or ('"' in t)
               for word in t.lower().split()]
    terms = [ps.stem(word) 
             for t in tokens 
             for word in t.lower().split() 
             if word not in phrases]
    
    return phrases, terms
