import { useState } from 'react';
import { Navigation } from './components/Navigation';
import { HomePage } from './components/HomePage';
import { BrowseMovies } from './components/BrowseMovies';
import { MovieDetail } from './components/MovieDetail';
import { UserProfile } from './components/UserProfile';
import { LoginPage } from './components/LoginPage';
import { FeedPage } from './components/FeedPage';
import { NotificationsPage } from './components/NotificationsPage';
import { WriteReview } from './components/WriteReview';
import { FollowersPage } from './components/FollowersPage';
import { Chatbot } from './components/Chatbot';
import { mockNotifications } from './lib/mockData';

export default function App() {
  const [isLoggedIn, setIsLoggedIn] = useState(false);
  const [currentPage, setCurrentPage] = useState('home');
  const [pageData, setPageData] = useState({});

  const unreadNotifications = mockNotifications.filter(n => !n.read).length;

  // Pages that require login
  const protectedPages = ['profile', 'user-profile', 'feed', 'notifications', 'write-review', 'followers', 'following'];

  const handleNavigate = (page, data) => {
    // If trying to access protected page while not logged in, redirect to login
    if (!isLoggedIn && protectedPages.includes(page)) {
      setCurrentPage('login');
      setPageData({ redirectTo: page, redirectData: data });
      window.scrollTo(0, 0);
      return;
    }
    setCurrentPage(page);
    setPageData(data || {});
    window.scrollTo(0, 0);
  };

  const handleLogin = () => {
    setIsLoggedIn(true);
    // If there was a redirect target, go there; otherwise go home
    if (pageData.redirectTo) {
      setCurrentPage(pageData.redirectTo);
      setPageData(pageData.redirectData || {});
    } else {
      setCurrentPage('home');
    }
  };

  const handleLogout = () => {
    setIsLoggedIn(false);
    setCurrentPage('home');
  };

  return (
    <div className="min-h-screen bg-background">
      <Navigation
        currentPage={currentPage}
        onNavigate={handleNavigate}
        isLoggedIn={isLoggedIn}
        onLogout={handleLogout}
        notificationCount={unreadNotifications}
      />

      {currentPage === 'home' && <HomePage onNavigate={handleNavigate} isLoggedIn={isLoggedIn} />}
      {currentPage === 'browse' && <BrowseMovies onNavigate={handleNavigate} />}
      {currentPage === 'movie' && pageData.id && (
        <MovieDetail movieId={pageData.id} onNavigate={handleNavigate} isLoggedIn={isLoggedIn} />
      )}
      {currentPage === 'login' && (
        <LoginPage onLogin={handleLogin} onCancel={() => handleNavigate('home')} />
      )}

      {/* Protected pages — only render if logged in */}
      {currentPage === 'profile' && isLoggedIn && <UserProfile onNavigate={handleNavigate} />}
      {currentPage === 'user-profile' && isLoggedIn && (
        <UserProfile userId={pageData.userId} onNavigate={handleNavigate} />
      )}
      {currentPage === 'feed' && isLoggedIn && <FeedPage onNavigate={handleNavigate} />}
      {currentPage === 'notifications' && isLoggedIn && (
        <NotificationsPage onNavigate={handleNavigate} />
      )}
      {currentPage === 'write-review' && isLoggedIn && (
        <WriteReview movieId={pageData.movieId} onNavigate={handleNavigate} />
      )}
      {(currentPage === 'followers' || currentPage === 'following') && isLoggedIn && (
        <FollowersPage userId={pageData.userId} onNavigate={handleNavigate} />
      )}

      {/* Chatbot only visible when logged in */}
      {isLoggedIn && <Chatbot />}
    </div>
  );
}
