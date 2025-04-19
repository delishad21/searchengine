import React, { useState } from "react";
import {
  Container,
  TextField,
  Button,
  Typography,
  Card,
  CardContent,
  Link,
  Chip,
  Grid,
  Divider,
  Box,
  Collapse,
  IconButton,
} from "@mui/material";
import ExpandMoreIcon from "@mui/icons-material/ExpandMore";
import ExpandLessIcon from "@mui/icons-material/ExpandLess";

function App() {
  const [query, setQuery] = useState("");
  const [results, setResults] = useState([]);
  const [expanded, setExpanded] = useState({}); // track which results are expanded

  const handleSearch = async () => {
    if (!query.trim()) return;

    try {
      const res = await fetch(
        `http://127.0.0.1:5000/search?q=${encodeURIComponent(query)}`
      );
      const data = await res.json();
      setResults(data);
      setExpanded({}); // Reset expansion state on new search
    } catch (err) {
      console.error("Search error:", err);
    }
  };

  const toggleExpand = (index) => {
    setExpanded((prev) => ({ ...prev, [index]: !prev[index] }));
  };

  return (
    <Container maxWidth="md" sx={{ py: 4 }}>
      <Typography variant="h4" gutterBottom>
        Search Engine
      </Typography>

      <Box display="flex" gap={2} mb={4}>
        <TextField
          label="Enter search query"
          fullWidth
          value={query}
          onChange={(e) => setQuery(e.target.value)}
        />
        <Button variant="contained" onClick={handleSearch}>
          Search
        </Button>
      </Box>

      {results.length === 0 && (
        <Typography>No results yet. Try a search!</Typography>
      )}

      {results.map((item, idx) => (
        <Card key={idx} sx={{ mb: 3 }}>
          <CardContent>
            <Typography variant="h6">
              <Link href={item.url} target="_blank" underline="hover">
                {item.title}
              </Link>
            </Typography>
            <Typography variant="body2" color="text.secondary" gutterBottom>
              <Link href={item.url} target="_blank">
                {item.url}
              </Link>
            </Typography>
            <Typography variant="subtitle2" color="text.secondary">
              Score: {item.score.toFixed(4)} | {item.metadata}
            </Typography>

            <Box my={1}>
              <Typography variant="body2" fontWeight="bold">
                Top Keywords:
              </Typography>
              {item.keywords.map((k, i) => (
                <Chip
                  key={i}
                  label={`${k.word} (${k.freq})`}
                  size="small"
                  sx={{ mr: 1, mt: 1 }}
                />
              ))}
            </Box>

            <Box display="flex" alignItems="center" mt={2}>
              <Typography variant="body2" fontWeight="bold">
                Parent & Child Links
              </Typography>
              <IconButton
                size="small"
                onClick={() => toggleExpand(idx)}
                sx={{ ml: 1 }}
              >
                {expanded[idx] ? <ExpandLessIcon /> : <ExpandMoreIcon />}
              </IconButton>
            </Box>

            <Collapse in={expanded[idx]} timeout="auto" unmountOnExit>
              <Divider sx={{ my: 1 }} />
              <Grid container spacing={2}>
                <Grid item xs={6}>
                  <Typography variant="body2" fontWeight="bold">
                    Parents
                  </Typography>
                  {item.parents.length > 0 ? (
                    item.parents.map((p, i) => (
                      <Typography variant="body2" key={i}>
                        <Link href={p} target="_blank" underline="hover">
                          {p}
                        </Link>
                      </Typography>
                    ))
                  ) : (
                    <Typography variant="body2">None</Typography>
                  )}
                </Grid>
                <Grid item xs={6}>
                  <Typography variant="body2" fontWeight="bold">
                    Children
                  </Typography>
                  {item.children.length > 0 ? (
                    item.children.map((c, i) => (
                      <Typography variant="body2" key={i}>
                        <Link href={c} target="_blank" underline="hover">
                          {c}
                        </Link>
                      </Typography>
                    ))
                  ) : (
                    <Typography variant="body2">None</Typography>
                  )}
                </Grid>
              </Grid>
            </Collapse>
          </CardContent>
        </Card>
      ))}
    </Container>
  );
}

export default App;
