from flask import Flask, request, jsonify
from flask_cors import CORS
from search_engine import search

app = Flask(__name__)
CORS(app)

@app.route("/search", methods=["GET"])
def search_route():
    query = request.args.get("q", "")
    if not query.strip():
        return jsonify({"error": "Query string is required"}), 400

    results = search(query)
    return jsonify(results)

if __name__ == "__main__":
    app.run(debug=True)
