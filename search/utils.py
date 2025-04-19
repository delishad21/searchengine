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
    tokens = shlex.split(query)
    phrases = [t.lower() for t in tokens if " " in t]
    terms = [ps.stem(word) for t in tokens for word in t.lower().split() if word not in phrases]
    return phrases, terms
