import { useState, useEffect } from 'react';
import { Heart, Share2, Star, Eye } from 'lucide-react';
import { Card, CardContent } from './ui/card';
import { Button } from './ui/button';
import { Badge } from './ui/badge';
import { Avatar, AvatarFallback, AvatarImage } from './ui/avatar';
import { ImageWithFallback } from './figma/ImageWithFallback';
import { useAppNavigate as useNavigate } from '../hooks/useAppNavigate';

const API_URL = 'http://localhost:8080/api';

const authHeaders = () => ({
  'Content-Type': 'application/json',
  'Authorization': `Bearer ${localStorage.getItem('token')}`
});

export function FeedPage() {
  const navigate = useNavigate();
  const isLoggedIn = !!localStorage.getItem('token');

  const [reviews, setReviews] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [page, setPage] = useState(0);
  const [loadingMore, setLoadingMore] = useState(false);
  const [hasMore, setHasMore] = useState(true);
  const [revealedSpoilers, setRevealedSpoilers] = useState(new Set());

  const fetchFeed = async (pageNum, append = false) => {
    try {
      if (append) setLoadingMore(true);
      else setLoading(true);
      setError(null);

      const res = await fetch(`${API_URL}/reviews/feed?page=${pageNum}&size=10`, {
        headers: authHeaders(),
      });
      if (!res.ok) throw new Error('Failed to fetch feed');
      const data = await res.json();

      if (data.length < 10) setHasMore(false);
      if (append) {
        setReviews((prev) => [...prev, ...data]);
      } else {
        setReviews(data);
        if (data.length < 10) setHasMore(false);
      }
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
      setLoadingMore(false);
    }
  };

  useEffect(() => {
    if (isLoggedIn) fetchFeed(0);
    else setLoading(false);
  }, [isLoggedIn]);

  const handleLoadMore = () => {
    const nextPage = page + 1;
    setPage(nextPage);
    fetchFeed(nextPage, true);
  };

  const handleLikeToggle = async (reviewId, currentlyLiked) => {
    // Optimistic update
    setReviews((prev) =>
      prev.map((r) =>
        r.id === reviewId
          ? {
              ...r,
              isLikedByMe: !currentlyLiked,
              likeCount: currentlyLiked ? r.likeCount - 1 : r.likeCount + 1,
            }
          : r
      )
    );
    try {
      const method = currentlyLiked ? 'DELETE' : 'POST';
      const res = await fetch(`${API_URL}/reviews/${reviewId}/like`, {
        method,
        headers: authHeaders(),
      });
      if (!res.ok) throw new Error('Failed');
    } catch {
      // Revert on failure
      setReviews((prev) =>
        prev.map((r) =>
          r.id === reviewId
            ? {
                ...r,
                isLikedByMe: currentlyLiked,
                likeCount: currentlyLiked ? r.likeCount + 1 : r.likeCount - 1,
              }
            : r
        )
      );
    }
  };

  const toggleSpoilerReveal = (reviewId) => {
    setRevealedSpoilers((prev) => {
      const next = new Set(prev);
      if (next.has(reviewId)) next.delete(reviewId);
      else next.add(reviewId);
      return next;
    });
  };

  if (!isLoggedIn) {
    return (
      <div className="min-h-screen bg-background flex items-center justify-center">
        <Card>
          <CardContent className="p-12 text-center">
            <p className="text-muted-foreground mb-4">Log in to see reviews from people you follow</p>
            <Button onClick={() => navigate('/login')}>
              Log In
            </Button>
          </CardContent>
        </Card>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-background">
      <div className="container mx-auto px-4 py-8 max-w-3xl">
        {/* Header */}
        <div className="mb-8">
          <h1 className="text-4xl font-bold mb-2 text-foreground">Your Feed</h1>
          <p className="text-muted-foreground">Reviews from people you follow</p>
        </div>

        {loading && (
          <div className="text-center py-12">
            <p className="text-muted-foreground">Loading your feed...</p>
          </div>
        )}

        {error && (
          <div className="text-center py-12">
            <p className="text-destructive">Error: {error}</p>
          </div>
        )}

        {!loading && !error && reviews.length === 0 && (
          <Card>
            <CardContent className="p-12 text-center">
              <p className="text-muted-foreground mb-4">
                Your feed is empty. Follow other users to see their reviews here.
              </p>
              <Button onClick={() => navigate('/browse')}>
                Browse Movies
              </Button>
            </CardContent>
          </Card>
        )}

        {!loading && !error && reviews.length > 0 && (
          <>
            {/* Feed Items */}
            <div className="space-y-4">
              {reviews.map((review) => (
                <Card key={review.id} className="hover:shadow-md hover:shadow-primary/5 transition-shadow hover:border-primary/30">
                  <CardContent className="p-6">
                    {/* User Info */}
                    <div className="flex items-center gap-3 mb-4">
                      <Avatar 
                        className="cursor-pointer"
                        onClick={() => navigate(`/profile/${review.username}`)}
                      >
                        <AvatarImage src={review.userAvatarUrl} alt={review.username} />
                        <AvatarFallback>{review.username[0]}</AvatarFallback>
                      </Avatar>
                      <div className="flex-1">
                        <p 
                          className="font-semibold cursor-pointer hover:underline text-foreground"
                          onClick={() => navigate(`/profile/${review.username}`)}
                        >
                          {review.userFullName || review.username}
                        </p>
                        <p className="text-sm text-muted-foreground">
                          reviewed a movie • {review.createdAt}
                        </p>
                      </div>
                    </div>

                    {/* Review Content */}
                    <div className="flex gap-4">
                      <ImageWithFallback
                        src={review.moviePosterUrl}
                        alt={review.movieTitle}
                        className="w-24 h-36 object-cover rounded cursor-pointer"
                        onClick={() => navigate(`/movie/${review.tmdbId}`)}
                      />
                      <div className="flex-1">
                        <div className="flex items-center gap-2 mb-2">
                          <h3 
                            className="text-xl font-bold cursor-pointer hover:text-primary text-foreground"
                            onClick={() => navigate(`/movie/${review.tmdbId}`)}
                          >
                            {review.movieTitle}
                          </h3>
                          {review.hasSpoilers && (
                            <Badge variant="destructive" className="text-xs">Spoiler</Badge>
                          )}
                        </div>
                        <div className="flex items-center gap-1 mb-3">
                          {[...Array(5)].map((_, i) => (
                            <Star
                              key={i}
                              className={`h-4 w-4 ${
                                i < review.rating
                                  ? 'fill-yellow-400 text-yellow-400'
                                  : 'text-muted-foreground/30'
                              }`}
                            />
                          ))}
                        </div>
                        <p className="font-semibold mb-2 text-foreground">{review.title}</p>
                        {review.hasSpoilers && !revealedSpoilers.has(review.id) ? (
                          <div className="relative">
                            <p className="text-muted-foreground text-sm select-none" style={{ filter: 'blur(8px)' }}>{review.content}</p>
                            <div className="absolute inset-0 flex items-center justify-center">
                              <Button
                                variant="outline"
                                size="sm"
                                onClick={() => toggleSpoilerReveal(review.id)}
                              >
                                <Eye className="h-4 w-4 mr-2" />
                                Reveal
                              </Button>
                            </div>
                          </div>
                        ) : (
                          <p className="text-muted-foreground text-sm">{review.content}</p>
                        )}
                      </div>
                    </div>

                    {/* Actions */}
                    <div className="flex items-center gap-1 mt-4 pt-4 border-t border-border">
                      <Button 
                        variant="ghost" 
                        size="sm"
                        className={review.isLikedByMe ? 'text-red-500' : ''}
                        onClick={() => handleLikeToggle(review.id, review.isLikedByMe)}
                      >
                        <Heart className={`h-4 w-4 mr-2 ${review.isLikedByMe ? 'fill-current' : ''}`} />
                        {review.likeCount}
                      </Button>
                      <Button variant="ghost" size="sm">
                        <Share2 className="h-4 w-4 mr-2" />
                        Share
                      </Button>
                    </div>
                  </CardContent>
                </Card>
              ))}
            </div>

            {/* Load More */}
            {hasMore && (
              <div className="text-center mt-8">
                <Button
                  variant="outline"
                  onClick={handleLoadMore}
                  disabled={loadingMore}
                >
                  {loadingMore ? 'Loading...' : 'Load More'}
                </Button>
              </div>
            )}
          </>
        )}
      </div>
    </div>
  );
}
