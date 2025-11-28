package br.gov.pa.parapaz.matriculacpu.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "spring.mail")
public class EmailConfig {
    
    private String username;
    private String host;
    private int port;
    private Properties properties;
    
    // Getters e Setters
    public String getUsername() { 
        return username; 
    }
    
    public void setUsername(String username) { 
        this.username = username; 
    }
    
    public String getHost() { 
        return host; 
    }
    
    public void setHost(String host) { 
        this.host = host; 
    }
    
    public int getPort() { 
        return port; 
    }
    
    public void setPort(int port) { 
        this.port = port; 
    }
    
    public Properties getProperties() { 
        return properties; 
    }
    
    public void setProperties(Properties properties) { 
        this.properties = properties; 
    }
    
    public static class Properties {
        private Mail mail;
        
        public Mail getMail() { 
            return mail; 
        }
        
        public void setMail(Mail mail) { 
            this.mail = mail; 
        }
        
        public static class Mail {
            private Smtp smtp;
            
            public Smtp getSmtp() { 
                return smtp; 
            }
            
            // ✅ CORREÇÃO AQUI: Nome do setter corrigido
            public void setSmtp(Smtp smtp) { 
                this.smtp = smtp; 
            }
            
            public static class Smtp {
                private boolean auth;
                private Starttls starttls;
                private int connectiontimeout;
                private int timeout;
                private int writetimeout;
                private boolean debug;
                
                public boolean isAuth() { 
                    return auth; 
                }
                
                public void setAuth(boolean auth) { 
                    this.auth = auth; 
                }
                
                public Starttls getStarttls() { 
                    return starttls; 
                }
                
                public void setStarttls(Starttls starttls) { 
                    this.starttls = starttls; 
                }
                
                public int getConnectiontimeout() { 
                    return connectiontimeout; 
                }
                
                public void setConnectiontimeout(int connectiontimeout) { 
                    this.connectiontimeout = connectiontimeout; 
                }
                
                public int getTimeout() { 
                    return timeout; 
                }
                
                public void setTimeout(int timeout) { 
                    this.timeout = timeout; 
                }
                
                public int getWritetimeout() { 
                    return writetimeout; 
                }
                
                public void setWritetimeout(int writetimeout) { 
                    this.writetimeout = writetimeout; 
                }
                
                public boolean isDebug() { 
                    return debug; 
                }
                
                public void setDebug(boolean debug) { 
                    this.debug = debug; 
                }
                
                public static class Starttls {
                    private boolean enable;
                    
                    public boolean isEnable() { 
                        return enable; 
                    }
                    
                    public void setEnable(boolean enable) { 
                        this.enable = enable; 
                    }
                }
            }
        }
    }
}