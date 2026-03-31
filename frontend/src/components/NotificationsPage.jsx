import { useState, useEffect, useCallback } from 'react';
import { Bell, Heart, UserPlus, Loader2 } from 'lucide-react';
import { Card, CardContent } from './ui/card';
import { Avatar, AvatarFallback, AvatarImage } from './ui/avatar';
import { Badge } from './ui/badge';

const API_URL = 'http://localhost:8080/api';

const authHeaders = () => ({
  'Content-Type': 'application/json',
  'Authorization': `Bearer ${localStorage.getItem('token')}`
});

const TABS = [
  { key: 'unread', label: 'Unread' },
  { key: 'all', label: 'All' },
  { key: 'read', label: 'Read' },
];

const CACHE_KEY = 'moventia_unread_notifs';
const CACHE_TTL_MS = 10 * 60 * 1000; // 10 minutes

/** Read cached unread notifications if still valid (< 10 min old). */
function getCachedUnread() {
  try {
    const raw = localStorage.getItem(CACHE_KEY);
    if (!raw) return null;
    const { data, timestamp } = JSON.parse(raw);
    if (Date.now() - timestamp < CACHE_TTL_MS) return data;
    localStorage.removeItem(CACHE_KEY);
  } catch {
    localStorage.removeItem(CACHE_KEY);
  }
  return null;
}

/** Store unread notifications in localStorage with a timestamp. */
function setCachedUnread(data) {
  try {
    localStorage.setItem(CACHE_KEY, JSON.stringify({ data, timestamp: Date.now() }));
  } catch {
    // storage full — ignore
  }
}

export function NotificationsPage({ onNotificationsRead }) {
  const [activeTab, setActiveTab] = useState('unread');
  const [notifications, setNotifications] = useState([]);
  const [loading, setLoading] = useState(true);
  const [markedRead, setMarkedRead] = useState(false);

  const getIcon = (type) => {
    switch (type) {
      case 'FOLLOW':
        return <UserPlus className="h-5 w-5 text-blue-500" />;
      case 'REVIEW_LIKE':
        return <Heart className="h-5 w-5 text-red-500" />;
      default:
        return <Bell className="h-5 w-5" />;
    }
  };

  // Fetch notifications for the active tab (with cache support for 'unread')
  const fetchNotifications = useCallback(async (filter) => {
    // Check cache first for unread tab
    if (filter === 'unread') {
      const cached = getCachedUnread();
      if (cached) {
        setNotifications(cached);
        setLoading(false);
        return;
      }
    }

    setLoading(true);
    try {
      const res = await fetch(`${API_URL}/notifications?filter=${filter}`, {
        headers: authHeaders(),
      });
      if (res.ok) {
        const data = await res.json();
        setNotifications(data);
        // Cache unread results
        if (filter === 'unread') setCachedUnread(data);
      } else {
        setNotifications([]);
      }
    } catch {
      setNotifications([]);
    } finally {
      setLoading(false);
    }
  }, []);

  // Mark all as read, then reset badge
  const markAllRead = useCallback(async () => {
    try {
      await fetch(`${API_URL}/notifications/mark-read`, {
        method: 'POST',
        headers: authHeaders(),
      });
      if (onNotificationsRead) onNotificationsRead();
    } catch {
      // silent
    }
  }, [onNotificationsRead]);

  // On first mount: mark-read → fetch active tab
  useEffect(() => {
    const init = async () => {
      await markAllRead();
      setMarkedRead(true);
      await fetchNotifications(activeTab);
    };
    init();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  // On tab change (after initial mount)
  useEffect(() => {
    if (markedRead) {
      fetchNotifications(activeTab);
    }
  }, [activeTab, markedRead, fetchNotifications]);

  return (
    <div className="min-h-screen bg-background">
      <div className="container mx-auto px-4 py-8 max-w-3xl">
        {/* Header */}
        <div className="mb-8">
          <h1 className="text-4xl font-bold mb-2 text-foreground">Notifications</h1>
          <p className="text-muted-foreground">Stay updated with your activity</p>
        </div>

        {/* Filter Tabs */}
        <div className="flex gap-2 mb-6">
          {TABS.map((tab) => (
            <Badge
              key={tab.key}
              variant={activeTab === tab.key ? 'default' : 'outline'}
              className="cursor-pointer"
              onClick={() => setActiveTab(tab.key)}
            >
              {tab.label}
            </Badge>
          ))}
        </div>

        {/* Loading State */}
        {loading && (
          <div className="flex items-center justify-center py-16">
            <Loader2 className="h-8 w-8 animate-spin text-primary" />
          </div>
        )}

        {/* Empty State */}
        {!loading && notifications.length === 0 && (
          <Card>
            <CardContent className="p-12 text-center">
              <Bell className="h-12 w-12 text-muted-foreground mx-auto mb-4" />
              <p className="text-muted-foreground mb-4">No notifications yet</p>
              <p className="text-sm text-muted-foreground">
                When people interact with your reviews or follow you, you'll see it here
              </p>
            </CardContent>
          </Card>
        )}

        {/* Notifications List */}
        {!loading && notifications.length > 0 && (
          <div className="space-y-2">
            {notifications.map((notification) => (
              <Card
                key={notification.id}
                className={`cursor-pointer hover:shadow-md hover:shadow-primary/5 transition-shadow ${
                  !notification.read ? 'border-l-4 border-l-primary bg-primary/5' : ''
                }`}
              >
                <CardContent className="p-4">
                  <div className="flex items-start gap-4">
                    <Avatar>
                      <AvatarImage
                        src={notification.actorAvatarUrl}
                        alt={notification.actorUsername}
                      />
                      <AvatarFallback>
                        {notification.actorUsername
                          ? notification.actorUsername[0].toUpperCase()
                          : '?'}
                      </AvatarFallback>
                    </Avatar>
                    <div className="flex-1">
                      <div className="flex items-start justify-between gap-2">
                        <p className="text-sm">
                          <span className="text-muted-foreground">{notification.message}</span>
                        </p>
                        <div className="flex-shrink-0">
                          {getIcon(notification.type)}
                        </div>
                      </div>
                      <p className="text-xs text-muted-foreground mt-1">
                        {notification.createdAt}
                      </p>
                    </div>
                  </div>
                </CardContent>
              </Card>
            ))}
          </div>
        )}
      </div>
    </div>
  );
}
