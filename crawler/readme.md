# Project README

### Web Search Engine Project

## Project Overview
Complete web search engine with:
1. Java crawler/indexer
2. Python search backend (Flask)
3. React frontend

## Project Structure

## Prerequisites
- JDK 11+
- Python 3.8+
- Node.js 14.x+
- Flask


## Building the Project

1.  **Navigate to the project root directory:**

    ```bash
    cd crawler
    ```

2.  **Build the project using Gradle:**

    * **On Linux/macOS:**

        ```bash
        ./gradlew build
        ```

    * **On Windows:**

        ```bash
        gradlew.bat build
        ```

    This command will compile the source code and create the necessary executable files in the `app/build/libs` directory.

## Running the Spider

1.  **Navigate to the project root directory:**

    ```bash
    cd crawler
    ```

2.  **Run the spider using Gradle:**

    * **On Linux/macOS:**

        ```bash
        ./gradlew run
        ```

      Example:

        ```bash
        ./gradlew run
        ```

    * **On Windows:**

        ```bash
        gradlew.bat run"
        ```

    The spider will fetch the specified number of pages (300 pages), index them, and store the data in `search_index.db`.


## Cleaning the Project

To clean the build files and database, run the following command from the project root directory:

* **On Linux/macOS:**

    ```bash
    ./gradlew clean
    ```

* **On Windows:**

    ```bash
    gradlew.bat clean
    ```

This will delete the `build` directory and the `search_index.db` and `search_index.lg` files.

## Running the search engine

* ### Frontend setup

    ```bash
    cd frontend/search-frontend/
    npm install    # Install React dependencies
    npm start      # Runs at http://localhost:3000 
    ```


* ### Backend setup


    ```bash
    cd search/
    pip install -r requirements.txt  # Install Flask, NLTK, Flask-Cors
    python app.py                   # Starts Flask at http://localhost:5000
    ```


## Notes

* Ensure that you have a stable internet connection while running the spider.
* The generated files will be found in `/crawler/app/`.
* The `spider_result.txt` file will be created or overwritten each time the test program is run.
* The `search_index.db` and `search_index.lg` files will be created or overwritten each time the spider is run.