import React from "react";
import { useState } from "react";
import {
  Container,
  TextField,
  Button,
  Typography,
  Link,
  Chip,
  Grid,
  Divider,
  Box,
  Collapse,
  CssBaseline,
  AppBar,
  Toolbar,
  Switch,
  FormControlLabel,
  Paper,
  Tabs,
  Tab,
  LinearProgress,
  List,
  ListItem,
  ListItemText,
  Tooltip,
  ThemeProvider,
  createTheme,
  IconButton,
  InputAdornment,
} from "@mui/material";
import {
  Search as SearchIcon,
  Info as InfoIcon,
  History as HistoryIcon,
  Analytics as AnalyticsIcon,
  OpenInNew as OpenInNewIcon,
  Clear as ClearIcon,
  Bookmark as BookmarkIcon,
  KeyboardArrowDown as KeyboardArrowDownIcon,
  KeyboardArrowUp as KeyboardArrowUpIcon,
  DarkMode as DarkModeIcon,
  LightMode as LightModeIcon,
} from "@mui/icons-material";
import hkust_logo from "./assets/images.png";
import { Snackbar, Alert } from "@mui/material";

export default function SearchEngine() {
  const [query, setQuery] = useState("");
  const [results, setResults] = useState([]);
  const [expanded, setExpanded] = useState({});
  const [isLoading, setIsLoading] = useState(false);
  const [searchHistory, setSearchHistory] = useState([]);
  const [tabValue, setTabValue] = useState(0);
  const [darkMode, setDarkMode] = useState(false);
  const [snackbar, setSnackbar] = useState({
    open: false,
    message: "",
    severity: "success",
  });
  const [isHomepage, setIsHomepage] = useState(true);

  const theme = createTheme({
    palette: {
      mode: darkMode ? "dark" : "light",
      primary: {
        main: "#1a73e8",
      },
      secondary: {
        main: "#ea4335",
      },
      background: {
        default: darkMode ? "#202124" : "#ffffff",
        paper: darkMode ? "#202124" : "#ffffff",
      },
    },
    typography: {
      fontFamily: "Roboto, Arial, sans-serif",
    },
    components: {
      MuiCard: {
        styleOverrides: {
          root: {
            boxShadow: "none",
            border: "none",
          },
        },
      },
      MuiTextField: {
        styleOverrides: {
          root: {
            "& .MuiOutlinedInput-root": {
              borderRadius: 24,
              "&:hover .MuiOutlinedInput-notchedOutline": {
                borderColor: darkMode
                  ? "rgba(255, 255, 255, 0.23)"
                  : "rgba(0, 0, 0, 0.23)",
              },
              "&.Mui-focused .MuiOutlinedInput-notchedOutline": {
                borderColor: "#1a73e8",
                borderWidth: 1,
              },
            },
          },
        },
      },
      MuiButton: {
        styleOverrides: {
          root: {
            textTransform: "none",
            borderRadius: 4,
          },
        },
      },
      MuiChip: {
        styleOverrides: {
          root: {
            borderRadius: 16,
          },
        },
      },
    },
  });

  const handleSearch = async () => {
    if (!query.trim()) return;

    setIsLoading(true);
    setIsHomepage(false);

    try {
      const res = await fetch(
        `http://127.0.0.1:5000/search?q=${encodeURIComponent(query)}`
      );
      const data = await res.json();

      setResults(data);

      setSearchHistory([query, ...searchHistory].slice(0, 10));

      setTabValue(0);
      setSnackbar({
        open: true,
        message: `Found ${data.length} results for "${query}"`,
        severity: "success",
      });
    } catch (err) {
      console.error("Search error:", err);
      setSnackbar({
        open: true,
        message: "There was an error processing your search. Please try again.",
        severity: "error",
      });
    } finally {
      setIsLoading(false);
    }
  };

  const toggleExpand = (index) => {
    setExpanded((prev) => ({ ...prev, [index]: !prev[index] }));
  };

  const handleSimilarSearch = (keywords) => {
    const newQuery = keywords
      .slice(0, 5)
      .map((k) => k.word)
      .join(" ");
    setQuery(newQuery);
    handleSearch();
  };

  const handleKeyDown = (e) => {
    if (e.key === "Enter") {
      handleSearch();
    }
  };

  const handleTabChange = (event, newValue) => {
    setTabValue(newValue);
  };

  const handleCloseSnackbar = () => {
    setSnackbar({ ...snackbar, open: false });
  };

  const handleClearSearch = () => {
    setQuery("");
  };

  const handleClickHomePage = () => {
    setIsHomepage(true);
    setQuery("");
  };

  return (
    <ThemeProvider theme={theme}>
      <CssBaseline />
      <Box
        sx={{ display: "flex", flexDirection: "column", minHeight: "100vh" }}
      >
        {isHomepage ? (
          // Homepage with centered logo and search
          <Container
            maxWidth="md"
            sx={{
              display: "flex",
              flexDirection: "column",
              alignItems: "center",
              justifyContent: "center",
              flexGrow: 1,
              py: 4,
              mt: 10,
            }}
          >
            <Box
              sx={{
                mb: 3,
                textAlign: "center",
                display: "flex",
                alignItems: "center",
                gap: 1,
              }}
            >
              <Typography
                variant="h2"
                component="h1"
                sx={{
                  fontWeight: 500,
                  color: theme.palette.mode === "dark" ? "#fff" : "#202124",
                  fontSize: { xs: "3rem", sm: "4rem" },
                  mb: 1,
                  display: "flex",
                  alignItems: "center",
                }}
              >
                <span style={{ color: "#4285F4" }}>H</span>
                <span style={{ color: "#EA4335" }}>K</span>
                <span style={{ color: "#FBBC05" }}>U</span>
                <span style={{ color: "#4285F4" }}>S</span>
                <span style={{ color: "#34A853" }}>T</span>
              </Typography>
              <img
                src={hkust_logo}
                alt="HKUST Logo"
                style={{
                  height: "3rem",
                  width: "3rem",
                  objectFit: "contain",
                }}
              />
            </Box>

            <Typography
              variant="subtitle1"
              color="textSecondary"
              sx={{ fontWeight: 300, mb: 4 }}
            >
              Search Engine Project
            </Typography>

            <Box sx={{ width: "100%", maxWidth: 584, mb: 4 }}>
              <TextField
                fullWidth
                variant="outlined"
                placeholder="Search the web"
                value={query}
                onChange={(e) => setQuery(e.target.value)}
                onKeyDown={handleKeyDown}
                InputProps={{
                  startAdornment: (
                    <InputAdornment position="start">
                      <SearchIcon color="action" />
                    </InputAdornment>
                  ),
                  sx: {
                    py: 1,
                    boxShadow: "0 1px 6px rgba(32,33,36,.28)",
                    borderRadius: 24,
                    "&:hover": {
                      boxShadow: "0 1px 10px rgba(32,33,36,.28)",
                    },
                  },
                }}
              />
            </Box>

            <Box sx={{ display: "flex", gap: 1 }}>
              <Button
                variant="contained"
                color="primary"
                onClick={handleSearch}
                sx={{
                  bgcolor:
                    theme.palette.mode === "dark"
                      ? "rgba(255,255,255,0.08)"
                      : "#f8f9fa",
                  color: theme.palette.mode === "dark" ? "#fff" : "#3c4043",
                  "&:hover": {
                    bgcolor:
                      theme.palette.mode === "dark"
                        ? "rgba(255,255,255,0.12)"
                        : "#f8f9fa",
                    boxShadow: "0 1px 1px rgba(0,0,0,0.1)",
                  },
                  boxShadow: "none",
                  px: 4,
                }}
              >
                HKUST Search
              </Button>
            </Box>

            <Box
              sx={{
                mt: "auto",
                width: "100%",
                display: "flex",
                justifyContent: "space-between",
                pt: 4,
              }}
            >
              <Box>
                <FormControlLabel
                  control={
                    <Switch
                      checked={darkMode}
                      onChange={() => setDarkMode(!darkMode)}
                      color="primary"
                    />
                  }
                  label={darkMode ? "Dark Mode" : "Light Mode"}
                />
              </Box>
              <Box>
                <Typography variant="body2" color="textSecondary">
                  © {new Date().getFullYear()} HKUST
                </Typography>
              </Box>
            </Box>
          </Container>
        ) : (
          // Search results page
          <>
            <AppBar
              position="sticky"
              elevation={0}
              sx={{
                bgcolor: theme.palette.background.default,
                borderBottom: 1,
                borderColor: "divider",
                color: theme.palette.text.primary,
              }}
            >
              <Toolbar
                sx={{ minHeight: { xs: 56, sm: 64 }, px: { xs: 1, sm: 2 } }}
              >
                <Box
                  sx={{ display: "flex", alignItems: "center", width: "100%" }}
                >
                  <Box
                    sx={{
                      display: "flex",
                      alignItems: "center",
                      mr: 2,
                      flexShrink: 0,
                    }}
                  >
                    <Typography
                      variant="h6"
                      component="div"
                      sx={{
                        fontWeight: 500,
                        fontSize: { xs: "1.1rem", sm: "1.25rem" },
                        display: "flex",
                        alignItems: "center",
                        cursor: "pointer",
                      }}
                      onClick={handleClickHomePage}
                    >
                      <span style={{ color: "#4285F4" }}>H</span>
                      <span style={{ color: "#EA4335" }}>K</span>
                      <span style={{ color: "#FBBC05" }}>U</span>
                      <span style={{ color: "#4285F4" }}>S</span>
                      <span style={{ color: "#34A853" }}>T</span>
                    </Typography>
                  </Box>

                  <Box sx={{ flexGrow: 1, maxWidth: 692 }}>
                    <TextField
                      fullWidth
                      variant="outlined"
                      size="small"
                      value={query}
                      onChange={(e) => setQuery(e.target.value)}
                      onKeyDown={handleKeyDown}
                      InputProps={{
                        startAdornment: (
                          <InputAdornment position="start">
                            <SearchIcon color="action" fontSize="small" />
                          </InputAdornment>
                        ),
                        endAdornment: (
                          <InputAdornment position="end">
                            <Box sx={{ display: "flex", alignItems: "center" }}>
                              {query && (
                                <IconButton
                                  size="small"
                                  onClick={handleClearSearch}
                                >
                                  <ClearIcon fontSize="small" />
                                </IconButton>
                              )}
                              <Divider
                                orientation="vertical"
                                flexItem
                                sx={{ mx: 0.5, height: 24 }}
                              />
                              <IconButton
                                size="small"
                                color="primary"
                                onClick={handleSearch}
                              >
                                <SearchIcon fontSize="small" />
                              </IconButton>
                            </Box>
                          </InputAdornment>
                        ),
                      }}
                      sx={{
                        "& .MuiOutlinedInput-root": {
                          borderRadius: 24,
                          bgcolor:
                            theme.palette.mode === "dark"
                              ? "rgba(255,255,255,0.08)"
                              : "#f1f3f4",
                          "& fieldset": { border: "none" },
                        },
                      }}
                    />
                  </Box>

                  <Box
                    sx={{ display: "flex", alignItems: "center", ml: "auto" }}
                  >
                    <Tooltip>
                      <IconButton
                        color="inherit"
                        size="large"
                        sx={{ ml: 1 }}
                        onClick={() => setDarkMode(!darkMode)}
                      >
                        {darkMode ? (
                          <LightModeIcon
                            sx={{
                              width: 32,
                              height: 32,
                            }}
                          ></LightModeIcon>
                        ) : (
                          <DarkModeIcon
                            sx={{
                              width: 32,
                              height: 32,
                            }}
                          ></DarkModeIcon>
                        )}
                      </IconButton>
                    </Tooltip>
                  </Box>
                </Box>
              </Toolbar>
            </AppBar>

            {isLoading && <LinearProgress sx={{ height: 3 }} />}

            <Container
              maxWidth="md"
              sx={{
                py: 2,
                px: { xs: 2, sm: 3 },
                display: "flex",
                flexDirection: "column",
                flexGrow: 1,
              }}
            >
              {/* Search stats */}
              {results.length > 0 && (
                <Typography
                  variant="body2"
                  color="textSecondary"
                  sx={{ mb: 2 }}
                >
                  {results.length} results shown
                </Typography>
              )}
              <Box sx={{ display: "flex", flexGrow: 1 }}>
                {
                  <Box
                    sx={{
                      width: 200,
                      flexShrink: 0,
                      pr: 3,
                      display: { xs: "none", md: "block" },
                    }}
                  >
                    <Tabs
                      value={tabValue}
                      onChange={handleTabChange}
                      orientation="vertical"
                      sx={{
                        borderRight: 1,
                        borderColor: "divider",
                        "& .MuiTab-root": {
                          alignItems: "flex-start",
                          textTransform: "none",
                          minHeight: 48,
                          pl: 0,
                        },
                      }}
                    >
                      <Tab
                        icon={<SearchIcon fontSize="small" />}
                        iconPosition="start"
                        label="Results"
                      />

                      <Tab
                        icon={<AnalyticsIcon fontSize="small" />}
                        iconPosition="start"
                        label="Analytics"
                      />

                      <Tab
                        icon={<HistoryIcon fontSize="small" />}
                        iconPosition="start"
                        label="History"
                      />
                    </Tabs>
                  </Box>
                }

                {/* Main content */}
                <Box sx={{ flexGrow: 1 }}>
                  {/* Results Tab */}
                  {tabValue === 0 && (
                    <Box>
                      {results.length === 0 && !isLoading && (
                        <Box
                          sx={{
                            display: "flex",
                            flexDirection: "column",
                            alignItems: "center",
                            py: 6,
                          }}
                        >
                          <Paper
                            sx={{
                              p: 2,
                              borderRadius: "50%",
                              bgcolor: "action.hover",
                            }}
                          >
                            <SearchIcon fontSize="large" color="action" />
                          </Paper>
                          <Typography variant="h6" sx={{ mt: 2 }}>
                            No results found
                          </Typography>
                          <Typography variant="body2" color="text.secondary">
                            Try different keywords or check your spelling
                          </Typography>
                        </Box>
                      )}

                      {results.map((item, idx) => (
                        <Box
                          key={idx}
                          sx={{
                            mb: 5,
                            "&:not(:last-child)": { pb: 2 },
                            borderBottom: 1,
                            borderColor: "divider",
                          }}
                        >
                          <Box
                            sx={{ display: "flex", flexDirection: "column" }}
                          >
                            {/* Title */}
                            <Typography
                              variant="h6"
                              component="h2"
                              sx={{
                                fontSize: "20px",
                                fontWeight: 400,
                                color: "#1a0dab",
                                mb: 0.5,
                                "&:hover": { textDecoration: "underline" },
                                ...(theme.palette.mode === "dark" && {
                                  color: "#8ab4f8",
                                }),
                              }}
                            >
                              <Link
                                href={item.url}
                                target="_blank"
                                rel="noopener noreferrer"
                                underline="none"
                                color="inherit"
                              >
                                {item.title}
                              </Link>

                              <Tooltip title="Score">
                                <Chip
                                  label={`${item.score.toFixed(4)}`}
                                  size="small"
                                  sx={{
                                    height: 20,
                                    fontSize: "11px",
                                    ml: 1,
                                    bgcolor:
                                      theme.palette.mode === "dark"
                                        ? "rgba(255,255,255,0.08)"
                                        : "rgba(0,0,0,0.08)",
                                  }}
                                />
                              </Tooltip>
                            </Typography>

                            {/* URL and metadata */}
                            <Box
                              sx={{
                                display: "flex",
                                alignItems: "center",
                                mb: 0.5,
                              }}
                            >
                              <Link
                                href={item.url}
                                target="_blank"
                                rel="noopener noreferrer"
                                color="primary"
                                sx={{
                                  fontSize: "13px",
                                  display: "inline-flex",
                                  alignItems: "center",
                                  gap: 0.5,
                                  "&:hover": {
                                    textDecoration: "underline",
                                  },
                                }}
                              >
                                {item.url}
                              </Link>
                            </Box>

                            <Typography
                              variant="body2"
                              color="text.secondary"
                              sx={{ fontSize: "13px" }}
                            >
                              {item.metadata}
                            </Typography>

                            {/* Snippet */}
                            <Typography
                              variant="body2"
                              color="text.primary"
                              sx={{
                                mb: 1,
                                lineHeight: 1.6,
                                fontSize: "14px",
                              }}
                            >
                              {item.snippet}
                            </Typography>

                            {/* Keywords */}
                            <Box
                              sx={{
                                display: "flex",
                                flexWrap: "wrap",
                                gap: 0.5,
                                mb: 1,
                              }}
                            >
                              {item.keywords.map((k, i) => (
                                <Chip
                                  key={i}
                                  label={`${k.word} (${k.freq})`}
                                  size="small"
                                  variant="outlined"
                                  onClick={() => {
                                    setQuery(k.word);
                                    handleSearch();
                                  }}
                                  clickable
                                  sx={{
                                    height: 24,
                                    fontSize: "12px",
                                    bgcolor:
                                      theme.palette.mode === "dark"
                                        ? "rgba(255,255,255,0.04)"
                                        : "rgba(0,0,0,0.04)",
                                    borderColor: "transparent",
                                  }}
                                />
                              ))}
                            </Box>

                            {/* Actions */}
                            <Box
                              sx={{
                                display: "flex",
                                gap: 1,
                                alignItems: "center",
                              }}
                            >
                              <Button
                                size="small"
                                startIcon={
                                  expanded[idx] ? (
                                    <KeyboardArrowUpIcon />
                                  ) : (
                                    <KeyboardArrowDownIcon />
                                  )
                                }
                                onClick={() => toggleExpand(idx)}
                                sx={{
                                  textTransform: "none",
                                  color: theme.palette.primary.main,
                                  fontWeight: 400,
                                  fontSize: "14px",
                                  "&:hover": {
                                    bgcolor: "transparent",
                                    textDecoration: "underline",
                                  },
                                }}
                              >
                                {expanded[idx]
                                  ? "Fewer details"
                                  : "More details"}
                              </Button>

                              <Button
                                size="small"
                                startIcon={<BookmarkIcon fontSize="small" />}
                                onClick={() =>
                                  handleSimilarSearch(item.keywords)
                                }
                                sx={{
                                  textTransform: "none",
                                  color: theme.palette.primary.main,
                                  fontWeight: 400,
                                  fontSize: "14px",
                                  "&:hover": {
                                    bgcolor: "transparent",
                                    textDecoration: "underline",
                                  },
                                }}
                              >
                                Find similar
                              </Button>
                            </Box>

                            {/* Expandable content */}
                            <Collapse
                              in={expanded[idx]}
                              timeout="auto"
                              unmountOnExit
                            >
                              <Box
                                sx={{
                                  mt: 2,
                                  pt: 2,
                                }}
                              >
                                <Grid container spacing={2}>
                                  <Grid item xs={12} md={6}>
                                    <Typography
                                      variant="subtitle2"
                                      gutterBottom
                                    >
                                      Parent Links
                                    </Typography>
                                    <Box
                                      sx={{ maxHeight: 150, overflow: "auto" }}
                                    >
                                      {item.parents.length > 0 ? (
                                        <List dense disablePadding>
                                          {item.parents.map((p, i) => (
                                            <ListItem
                                              key={i}
                                              disablePadding
                                              sx={{ py: 0.5 }}
                                            >
                                              <ListItemText
                                                primary={
                                                  <Link
                                                    href={p}
                                                    target="_blank"
                                                    rel="noopener noreferrer"
                                                    color="primary"
                                                    sx={{
                                                      fontSize: "13px",
                                                      display: "inline-flex",
                                                      alignItems: "center",
                                                      gap: 0.5,
                                                      "&:hover": {
                                                        textDecoration:
                                                          "underline",
                                                      },
                                                    }}
                                                  >
                                                    {p}
                                                    <OpenInNewIcon
                                                      sx={{ fontSize: 12 }}
                                                    />
                                                  </Link>
                                                }
                                              />
                                            </ListItem>
                                          ))}
                                        </List>
                                      ) : (
                                        <Typography
                                          variant="body2"
                                          color="text.secondary"
                                        >
                                          None
                                        </Typography>
                                      )}
                                    </Box>
                                  </Grid>

                                  <Grid item xs={12} md={6}>
                                    <Typography
                                      variant="subtitle2"
                                      gutterBottom
                                    >
                                      Child Links
                                    </Typography>
                                    <Box
                                      sx={{ maxHeight: 150, overflow: "auto" }}
                                    >
                                      {item.children.length > 0 ? (
                                        <List dense disablePadding>
                                          {item.children.map((c, i) => (
                                            <ListItem
                                              key={i}
                                              disablePadding
                                              sx={{ py: 0.5 }}
                                            >
                                              <ListItemText
                                                primary={
                                                  <Link
                                                    href={c}
                                                    target="_blank"
                                                    rel="noopener noreferrer"
                                                    color="primary"
                                                    sx={{
                                                      fontSize: "13px",
                                                      display: "inline-flex",
                                                      alignItems: "center",
                                                      gap: 0.5,
                                                      "&:hover": {
                                                        textDecoration:
                                                          "underline",
                                                      },
                                                    }}
                                                  >
                                                    {c}
                                                    <OpenInNewIcon
                                                      sx={{ fontSize: 12 }}
                                                    />
                                                  </Link>
                                                }
                                              />
                                            </ListItem>
                                          ))}
                                        </List>
                                      ) : (
                                        <Typography
                                          variant="body2"
                                          color="text.secondary"
                                        >
                                          None
                                        </Typography>
                                      )}
                                    </Box>
                                  </Grid>
                                </Grid>
                              </Box>
                            </Collapse>
                          </Box>
                        </Box>
                      ))}
                    </Box>
                  )}

                  {/* History Tab */}
                  {tabValue === 2 && (
                    <Paper
                      variant="outlined"
                      sx={{
                        p: 2,
                        borderRadius: 1,
                      }}
                    >
                      <Typography variant="h6" gutterBottom>
                        Search History
                      </Typography>
                      {searchHistory.length > 0 ? (
                        <List sx={{ maxHeight: "60vh", overflow: "auto" }}>
                          {searchHistory.map((q, i) => (
                            <ListItem
                              key={i}
                              sx={{
                                py: 1.5,
                                borderBottom:
                                  i < searchHistory.length - 1 ? 1 : 0,
                                borderColor: "divider",
                              }}
                            >
                              <ListItemText
                                primary={q}
                                primaryTypographyProps={{
                                  fontWeight: 500,
                                  color: theme.palette.primary.main,
                                }}
                              />
                            </ListItem>
                          ))}
                        </List>
                      ) : (
                        <Typography
                          align="center"
                          color="text.secondary"
                          sx={{ py: 4 }}
                        >
                          No search history yet
                        </Typography>
                      )}
                    </Paper>
                  )}

                  {/* Analytics Tab */}
                  {tabValue === 1 && (
                    <Paper variant="outlined" sx={{ p: 2, borderRadius: 1 }}>
                      <Box
                        sx={{
                          display: "flex",
                          alignItems: "center",
                          gap: 1,
                          mb: 3,
                        }}
                      >
                        <Typography variant="h6">Search Analytics</Typography>
                        <Tooltip title="Statistics about your search results">
                          <InfoIcon fontSize="small" color="action" />
                        </Tooltip>
                      </Box>

                      {results.length > 0 ? (
                        <Box
                          sx={{
                            display: "flex",
                            flexDirection: "column",
                            gap: 4,
                          }}
                        >
                          <Box>
                            <Typography variant="subtitle1" gutterBottom>
                              Score Distribution
                            </Typography>
                            <Box sx={{ mt: 2 }}>
                              {results.map((item, idx) => (
                                <Box key={idx} sx={{ mb: 2 }}>
                                  <Box
                                    sx={{
                                      display: "flex",
                                      justifyContent: "space-between",
                                      mb: 0.5,
                                    }}
                                  >
                                    <Typography variant="body2">
                                      {item.title.substring(0, 30)}...
                                    </Typography>
                                    <Typography variant="body2">
                                      {item.score.toFixed(4)}
                                    </Typography>
                                  </Box>
                                  <LinearProgress
                                    variant="determinate"
                                    value={item.score * 100}
                                    sx={{
                                      height: 8,
                                      borderRadius: 1,
                                      bgcolor:
                                        theme.palette.mode === "dark"
                                          ? "rgba(255,255,255,0.08)"
                                          : "rgba(0,0,0,0.08)",
                                    }}
                                  />
                                </Box>
                              ))}
                            </Box>
                          </Box>

                          <Box>
                            <Typography variant="subtitle1" gutterBottom>
                              Top Keywords Across Results
                            </Typography>
                            <Box
                              sx={{
                                display: "flex",
                                flexWrap: "wrap",
                                gap: 1,
                                mt: 2,
                              }}
                            >
                              {Array.from(
                                results
                                  .flatMap((r) => r.keywords)
                                  .reduce((acc, { word, freq }) => {
                                    acc.set(word, (acc.get(word) || 0) + freq);
                                    return acc;
                                  }, new Map())
                              )
                                .sort((a, b) => b[1] - a[1])
                                .slice(0, 10)
                                .map(([word, freq]) => (
                                  <Chip
                                    key={word}
                                    label={`${word} (${freq})`}
                                    variant="outlined"
                                    onClick={() => setQuery(word)}
                                    clickable
                                    sx={{
                                      bgcolor:
                                        theme.palette.mode === "dark"
                                          ? "rgba(255,255,255,0.04)"
                                          : "rgba(0,0,0,0.04)",
                                      borderColor: "transparent",
                                    }}
                                  />
                                ))}
                            </Box>
                          </Box>
                        </Box>
                      ) : (
                        <Typography
                          align="center"
                          color="text.secondary"
                          sx={{ py: 4 }}
                        >
                          No data to analyze yet. Try searching first!
                        </Typography>
                      )}
                    </Paper>
                  )}
                </Box>
              </Box>
            </Container>

            <Box
              component="footer"
              sx={{
                py: 2,
                px: 2,
                mt: "auto",
                bgcolor: theme.palette.background.paper,
                borderTop: 1,
                borderColor: "divider",
              }}
            >
              <Container maxWidth="lg">
                <Grid
                  container
                  justifyContent="space-between"
                  alignItems="center"
                >
                  <Grid item>
                    <Typography variant="body2" color="text.secondary">
                      COMP4321 Search Engine Project
                    </Typography>
                  </Grid>
                  <Grid item>
                    <Typography variant="body2" color="text.secondary">
                      © {new Date().getFullYear()} HKUST
                    </Typography>
                  </Grid>
                </Grid>
              </Container>
            </Box>
          </>
        )}

        <Snackbar
          open={snackbar.open}
          autoHideDuration={4000}
          onClose={handleCloseSnackbar}
          anchorOrigin={{ vertical: "bottom", horizontal: "center" }}
        >
          <Alert
            onClose={handleCloseSnackbar}
            severity={snackbar.severity}
            sx={{ width: "100%" }}
          >
            {snackbar.message}
          </Alert>
        </Snackbar>
      </Box>
    </ThemeProvider>
  );
}
