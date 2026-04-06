import { useState, useEffect } from 'react';
import { Star, Heart } from 'lucide-react';
import { Card, CardContent } from './ui/card';
import { Badge } from './ui/badge';
import { ImageWithFallback } from './figma/ImageWithFallback';
import { useAppNavigate as useNavigate } from '../hooks/useAppNavigate';

const API_URL = 'http://localhost:8080/api';

export function MovieCard({ movie, isLoggedIn }) {
  const navigate = useNavigate();
  const [isFavorited, setIsFavorited] = useState(false);

  useEffect(() => {
    const fetchFavoriteStatus = async () => {
      try {
        const token = localStorage.getItem('token');
        if (!token) return;
        const res = await fetch(`${API_URL}/favorites/status/${movie.tmdbId}`, {
          headers: { Authorization: `Bearer ${token}` }
        });
        if (res.ok) {
          const data = await res.json();
          setIsFavorited(data.favorited);
        }
      } catch {
        // silent fail
      }
    };

    if (isLoggedIn && movie?.tmdbId) {
      fetchFavoriteStatus();
    }
  }, [movie.tmdbId, isLoggedIn]);

  const toggleFavorite = async (e) => {
    e.stopPropagation();
    if (!isLoggedIn) {
      navigate('/login');
      return;
    }

    const currentlyFavorited = isFavorited;
    setIsFavorited(!currentlyFavorited); // Optimistic

    try {
      const token = localStorage.getItem('token');
      const method = currentlyFavorited ? 'DELETE' : 'POST';
      const res = await fetch(`${API_URL}/favorites/${movie.tmdbId}`, {
        method,
        headers: {
          'Content-Type': 'application/json',
          Authorization: `Bearer ${token}`
        }
      });
      
      if (!res.ok) {
        throw new Error('Failed to toggle favorite');
      }
    } catch {
      // Revert on error
      setIsFavorited(currentlyFavorited);
    }
  };

  return (
    <Card
      className="overflow-hidden cursor-pointer hover:shadow-lg hover:shadow-primary/5 transition-all hover:scale-105 hover:border-primary/30 h-full flex flex-col"
      onClick={() => navigate(`/movie/${movie.tmdbId}`)}
    >
      <div className="aspect-[2/3] relative">
        <ImageWithFallback
          src={movie.posterUrl}
          alt={movie.title}
          className="w-full h-full object-cover"
        />
        <div className="absolute top-2 right-2 bg-black/80 text-white px-2 py-1 rounded flex items-center gap-1 z-10 text-sm font-semibold">
          <Star className="h-4 w-4 fill-yellow-400 text-yellow-400" />
          <span>{movie.tmdbRating}</span>
        </div>
        
        {/* Favorite Icon */}
        <div 
          role="button"
          tabIndex={0}
          className={`absolute top-2 left-2 p-2 rounded-full backdrop-blur-md transition-colors z-10 
            ${isFavorited ? 'bg-primary/20 text-primary hover:bg-primary/30' : 'bg-black/50 text-white hover:bg-black/70 hover:text-primary'}
          `}
          onClick={toggleFavorite}
          onKeyDown={(e) => { if (e.key === 'Enter' || e.key === ' ') toggleFavorite(e); }}
        >
          <Heart className={`h-4 w-4 ${isFavorited ? 'fill-current' : ''}`} />
        </div>
      </div>

      <CardContent className="p-4 flex-1 flex flex-col">
        <h3 className="font-semibold mb-1 line-clamp-1 text-foreground">
          {movie.title}
        </h3>
        <p className="text-sm text-muted-foreground mb-2">
          {movie.releaseDate?.substring(0, 4)}
        </p>
        <div className="flex flex-wrap gap-1 mt-auto">
          {movie.genres?.slice(0, 2).map((g) => (
            <Badge key={g} variant="secondary" className="text-xs">
              {g}
            </Badge>
          ))}
        </div>
      </CardContent>
    </Card>
  );
}
