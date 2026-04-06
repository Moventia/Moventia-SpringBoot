import { useState, useEffect, useRef } from 'react';
import { MessageCircle, X, Send, Loader2, Sparkles } from 'lucide-react';
import { Button } from './ui/button';
import { Card } from './ui/card';
import { Input } from './ui/input';

export function Chatbot({ user }) {
  const [isOpen, setIsOpen] = useState(false);
  const [messages, setMessages] = useState([
    {
      id: '1',
      text: "Hi! I'm your movie assistant. I can help you find movies, get recommendations, or answer questions about films. How can I help you today?",
      sender: 'bot',
      timestamp: new Date()
    }
  ]);
  const [inputValue, setInputValue] = useState('');
  const [isLoading, setIsLoading] = useState(false);
  const [watchedMovies, setWatchedMovies] = useState([]);
  const scrollRef = useRef(null);
  const API_URL = 'http://localhost:8080/api';

  useEffect(() => {
    if (user?.username) {
      const fetchMovies = async () => {
        try {
          const token = localStorage.getItem('token');
          const res = await fetch(`${API_URL}/reviews/user/${encodeURIComponent(user.username)}`, {
            headers: { 'Authorization': `Bearer ${token}` }
          });
          if (res.ok) {
            const data = await res.json();
            const mapped = data.map(r => ({
              title: r.movieTitle,
              rating: r.rating,
              reviewTitle: r.title
            }));
            setWatchedMovies(mapped);
          }
        } catch (error) {
          console.error('Failed to fetch user movies for chatbot context', error);
        }
      };
      fetchMovies();
    }
  }, [user?.username]);

  const scrollToBottom = () => {
    if (scrollRef.current) {
      scrollRef.current.scrollTop = scrollRef.current.scrollHeight;
    }
  };

  useEffect(() => {
    setTimeout(scrollToBottom, 50);
  }, [messages, isLoading]);

  const handleSend = async () => {
    if (!inputValue.trim() || isLoading) return;

    const userText = inputValue;
    const userMessage = {
      id: Date.now().toString(),
      text: userText,
      sender: 'user',
      timestamp: new Date()
    };

    setMessages((prev) => [...prev, userMessage]);
    setInputValue('');
    setIsLoading(true);

    try {
      const token = localStorage.getItem('token');
      
      const history = messages.map(m => ({
        role: m.sender === 'bot' ? 'model' : 'user',
        content: m.text
      }));

      const payload = {
        message: userText,
        history: history,
        watchedMovies: watchedMovies
      };

      const res = await fetch(`${API_URL}/chat`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${token}`
        },
        body: JSON.stringify(payload)
      });

      if (!res.ok) throw new Error('Failed to get chat response');
      
      const data = await res.json();
      
      const botMessage = {
        id: (Date.now() + 1).toString(),
        text: data.response,
        sender: 'bot',
        timestamp: new Date()
      };
      
      setMessages((prev) => [...prev, botMessage]);
    } catch (error) {
      console.error('Chat error:', error);
      const errorMessage = {
        id: (Date.now() + 1).toString(),
        text: "Sorry, I'm having trouble connecting to the server. Please try again later.",
        sender: 'bot',
        timestamp: new Date()
      };
      setMessages((prev) => [...prev, errorMessage]);
    } finally {
      setIsLoading(false);
    }
  };

  if (!isOpen) {
    return (
      <Button
        onClick={() => setIsOpen(true)}
        size="lg"
        className="fixed bottom-6 right-6 h-14 w-14 rounded-full shadow-lg"
      >
        <MessageCircle className="h-6 w-6" />
      </Button>
    );
  }

  return (
    <>
      <style>{`
        .chatbot-scroll::-webkit-scrollbar {
          width: 6px;
        }
        .chatbot-scroll::-webkit-scrollbar-track {
          background: transparent;
        }
        .chatbot-scroll::-webkit-scrollbar-thumb {
          background: rgba(196, 156, 85, 0.15);
          border-radius: 999px;
        }
        .chatbot-scroll::-webkit-scrollbar-thumb:hover {
          background: rgba(196, 156, 85, 0.3);
        }
      `}</style>
      <Card className="fixed bottom-6 right-6 w-96 shadow-2xl overflow-hidden" style={{ height: '520px', display: 'flex', flexDirection: 'column' }}>
        {/* Header */}
        <div className="flex items-center justify-between p-4 border-b" style={{ flexShrink: 0, background: 'var(--background)' }}>
          <div className="flex items-center gap-2">
            <div style={{
              width: '32px', height: '32px', borderRadius: '50%',
              background: 'linear-gradient(135deg, #c49c55 0%, #a07830 100%)',
              display: 'flex', alignItems: 'center', justifyContent: 'center',
              flexShrink: 0,
            }}>
              <Sparkles size={16} style={{ color: '#fff' }} />
            </div>
            <div>
              <p className="font-semibold text-foreground" style={{ fontSize: '0.9rem' }}>Movie Assistant</p>
              <p className="text-muted-foreground" style={{ fontSize: '0.7rem' }}>Powered by Gemini</p>
            </div>
          </div>
          <Button variant="ghost" size="icon" onClick={() => setIsOpen(false)}>
            <X className="h-4 w-4" />
          </Button>
        </div>

        {/* Messages area */}
        <div
          ref={scrollRef}
          className="chatbot-scroll"
          style={{
            flex: 1, overflowY: 'auto', padding: '1rem',
            display: 'flex', flexDirection: 'column', gap: '0.75rem',
          }}
        >
          {messages.map((message) => (
            <div
              key={message.id}
              style={{
                display: 'flex',
                justifyContent: message.sender === 'user' ? 'flex-end' : 'flex-start',
              }}
            >
              {message.sender === 'bot' && (
                <div style={{
                  width: '24px', height: '24px', borderRadius: '50%',
                  background: 'linear-gradient(135deg, #c49c55 0%, #a07830 100%)',
                  display: 'flex', alignItems: 'center', justifyContent: 'center',
                  flexShrink: 0, marginRight: '8px', marginTop: '4px',
                }}>
                  <Sparkles size={12} style={{ color: '#fff' }} />
                </div>
              )}
              <div
                className={`rounded-lg p-3 ${
                  message.sender === 'user'
                    ? 'bg-primary text-primary-foreground'
                    : 'bg-muted'
                }`}
                style={{ maxWidth: '80%', minWidth: 0 }}
              >
                <p className="text-sm" style={{ whiteSpace: 'pre-wrap', wordBreak: 'break-word' }}>{message.text}</p>
              </div>
            </div>
          ))}
          {isLoading && (
            <div style={{ display: 'flex', justifyContent: 'flex-start' }}>
              <div style={{
                width: '24px', height: '24px', borderRadius: '50%',
                background: 'linear-gradient(135deg, #c49c55 0%, #a07830 100%)',
                display: 'flex', alignItems: 'center', justifyContent: 'center',
                flexShrink: 0, marginRight: '8px', marginTop: '4px',
              }}>
                <Sparkles size={12} style={{ color: '#fff' }} />
              </div>
              <div className="rounded-lg p-3 bg-muted">
                <Loader2 className="h-4 w-4 animate-spin text-muted-foreground" />
              </div>
            </div>
          )}
        </div>

        {/* Input area */}
        <div className="p-4 border-t" style={{ flexShrink: 0, background: 'var(--background)' }}>
          <div className="flex gap-2">
            <Input
              placeholder="Ask me anything about movies..."
              value={inputValue}
              onChange={(e) => setInputValue(e.target.value)}
              onKeyDown={(e) => e.key === 'Enter' && handleSend()}
            />
            <Button onClick={handleSend} size="icon" disabled={isLoading}>
              <Send className="h-4 w-4" />
            </Button>
          </div>
        </div>
      </Card>
    </>
  );
}
