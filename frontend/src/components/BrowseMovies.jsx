import { useState, useEffect, useCallback } from 'react';
import { Search, Star } from 'lucide-react';
import { Card, CardContent } from './ui/card';
import { Input } from './ui/input';
import { Button } from './ui/button';
import { Badge } from './ui/badge';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue
} from './ui/select';
import { ImageWithFallback } from './figma/ImageWithFallback';
import { useAppNavigate as useNavigate } from '../hooks/useAppNavigate';

const API_URL = 'http://localhost:8080/api';

const LANGUAGES = [
  { label: 'All', value: 'all' },
  { label: 'Hollywood', value: 'en' },
  { label: 'Bollywood', value: 'hi' },
  { label: 'Tamil', value: 'ta' },
  { label: 'Telugu', value: 'te' },
];

const SORT_OPTIONS = [
  { label: 'Popular', value: 'popularity.desc' },
  { label: 'Top Rated', value: 'vote_average.desc' },
  { label: 'Newest', value: 'release_date.desc' },
];

export function BrowseMovies() {
  const navigate = useNavigate();
  const [searchQuery, setSearchQuery] = useState('');
  const [language, setLanguage] = useState('all');
  const [sortBy, setSortBy] = useState('popularity.desc');
  const [movies, setMovies] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  const fetchMovies = useCallback(async (query, lang, sort) => {
    try {
      setLoading(true);
      setError(null);

      let url;
      if (query.trim()) {
        url = `${API_URL}/movies/search?q=${encodeURIComponent(query.trim())}&page=1`;
      } else {
        const params = new URLSearchParams({ page: '1', sortBy: sort });
        if (lang !== 'all') params.set('language', lang);
        url = `${API_URL}/movies/discover?${params.toString()}`;
      }

      const res = await fetch(url);
      if (!res.ok) throw new Error('Failed to fetch movies');
      const data = await res.json();
      setMovies(data);
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  }, []);

  // Debounced fetch when search/filters change
  useEffect(() => {
    const timer = setTimeout(() => {
      fetchMovies(searchQuery, language, sortBy);
    }, searchQuery ? 300 : 0);
    return () => clearTimeout(timer);
  }, [searchQuery, language, sortBy, fetchMovies]);

  return (
    <div className="min-h-screen bg-background">
      <div className="container mx-auto px-4 py-8">

        <div className="mb-8">
          <h1 className="text-4xl font-bold mb-2 text-foreground">Browse Movies</h1>
          <p className="text-muted-foreground">
            Discover your next favorite film
          </p>
        </div>

        <Card className="mb-8">
          <CardContent className="p-6">
            <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
              <div className="md:col-span-2 relative">
                <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 h-4 w-4 text-muted-foreground" />
                <Input
                  placeholder="Search movies..."
                  value={searchQuery}
                  onChange={(e) => setSearchQuery(e.target.value)}
                  className="pl-10"
                />
              </div>

              <div className="flex gap-2">
                <Select value={sortBy} onValueChange={setSortBy}>
                  <SelectTrigger>
                    <SelectValue placeholder="Sort by" />
                  </SelectTrigger>
                  <SelectContent>
                    {SORT_OPTIONS.map((opt) => (
                      <SelectItem key={opt.value} value={opt.value}>
                        {opt.label}
                      </SelectItem>
                    ))}
                  </SelectContent>
                </Select>
              </div>
            </div>

            <div className="mt-4 flex flex-wrap gap-2">
              {LANGUAGES.map((lang) => (
                <Badge
                  key={lang.value}
                  variant={language === lang.value ? 'default' : 'outline'}
                  className="cursor-pointer"
                  onClick={() => setLanguage(lang.value)}
                >
                  {lang.label}
                </Badge>
              ))}
            </div>
          </CardContent>
        </Card>

        {loading && (
          <div className="text-center py-12">
            <p className="text-muted-foreground">Loading movies...</p>
          </div>
        )}

        {error && (
          <div className="text-center py-12">
            <p className="text-destructive">Error: {error}</p>
          </div>
        )}

        {!loading && !error && (
          <>
            <div className="mb-4">
              <p className="text-muted-foreground">
                {movies.length}{' '}
                {movies.length === 1 ? 'movie' : 'movies'} found
              </p>
            </div>

            <div className="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-4 xl:grid-cols-5 gap-6">
              {movies.map((movie) => (
                <Card
                  key={movie.tmdbId}
                  className="overflow-hidden cursor-pointer hover:shadow-lg hover:shadow-primary/5 transition-all hover:scale-105 hover:border-primary/30"
                  onClick={() => navigate(`/movie/${movie.tmdbId}`)}
                >
                  <div className="aspect-[2/3] relative">
                    <ImageWithFallback
                      src={movie.posterUrl}
                      alt={movie.title}
                      className="w-full h-full object-cover"
                    />
                    <div className="absolute top-2 right-2 bg-black/80 text-white px-2 py-1 rounded flex items-center gap-1">
                      <Star className="h-4 w-4 fill-yellow-400 text-yellow-400" />
                      <span className="text-sm font-semibold">
                        {movie.tmdbRating}
                      </span>
                    </div>
                  </div>

                  <CardContent className="p-4">
                    <h3 className="font-semibold mb-1 line-clamp-1 text-foreground">
                      {movie.title}
                    </h3>
                    <p className="text-sm text-muted-foreground mb-2">
                      {movie.releaseDate?.substring(0, 4)}
                    </p>
                    <div className="flex flex-wrap gap-1">
                      {movie.genres?.slice(0, 2).map((g) => (
                        <Badge key={g} variant="secondary" className="text-xs">
                          {g}
                        </Badge>
                      ))}
                    </div>
                  </CardContent>
                </Card>
              ))}
            </div>

            {movies.length === 0 && (
              <div className="text-center py-12">
                <p className="text-muted-foreground mb-4">
                  No movies found matching your criteria
                </p>
                <Button
                  onClick={() => {
                    setSearchQuery('');
                    setLanguage('all');
                  }}
                >
                  Clear Filters
                </Button>
              </div>
            )}
          </>
        )}
      </div>
    </div>
  );
}
