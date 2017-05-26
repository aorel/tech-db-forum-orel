CREATE EXTENSION IF NOT EXISTS CITEXT;

CREATE TABLE IF NOT EXISTS users (
  id SERIAL NOT NULL PRIMARY KEY,
  nickname CITEXT NOT NULL UNIQUE,
  fullname CITEXT,
  email CITEXT NOT NULL UNIQUE,
  about TEXT
);
CREATE INDEX IF NOT EXISTS idx_users_nickname ON users (LOWER(nickname));


CREATE TABLE IF NOT EXISTS forums (
  id SERIAL NOT NULL PRIMARY KEY,
  title CITEXT NOT NULL,
  user_id INT NOT NULL,
  slug CITEXT NOT NULL UNIQUE,
  __posts INTEGER DEFAULT 0,
  __threads INTEGER DEFAULT 0,
  FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);
CREATE INDEX IF NOT EXISTS idx_forums_user_id ON forums (user_id);
CREATE INDEX IF NOT EXISTS idx_forums_slug ON forums (LOWER(slug));


CREATE TABLE IF NOT EXISTS threads (
  id SERIAL NOT NULL PRIMARY KEY,
  title CITEXT NOT NULL,
  user_id INT NOT NULL,
  forum_id INT NOT NULL,
  message CITEXT NOT NULL,
  slug CITEXT UNIQUE,
  created TIMESTAMP DEFAULT NOW(),
  __votes INT NOT NULL DEFAULT 0,
  FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
  FOREIGN KEY (forum_id) REFERENCES forums (id) ON DELETE CASCADE
);
CREATE INDEX IF NOT EXISTS idx_threads_user_id ON threads (user_id);
CREATE INDEX IF NOT EXISTS idx_threads_forum_id ON threads (forum_id);
CREATE INDEX IF NOT EXISTS idx_threads_slug ON threads (LOWER(slug));


CREATE TABLE IF NOT EXISTS votes (
  id SERIAL NOT NULL PRIMARY KEY,
  user_id INT NOT NULL,
  thread_id INT NOT NULL,
  voice SMALLINT,
  FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
  FOREIGN KEY (thread_id) REFERENCES threads (id) ON DELETE CASCADE,
  UNIQUE (user_id, thread_id)
);


CREATE TABLE IF NOT EXISTS posts (
  id SERIAL NOT NULL PRIMARY KEY,
  parent_id INT NOT NULL DEFAULT 0,
  user_id INT NOT NULL,
  forum_id INT NOT NULL,
  thread_id INT NOT NULL,

  is_edited BOOLEAN DEFAULT FALSE,
  message CITEXT NOT NULL,
  created TIMESTAMP DEFAULT NOW(),

  __nickname CITEXT NOT NULL,
  __path INT ARRAY,

  FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
  FOREIGN KEY (forum_id) REFERENCES forums (id) ON DELETE CASCADE,
  FOREIGN KEY (thread_id) REFERENCES threads (id) ON DELETE CASCADE
);
CREATE INDEX IF NOT EXISTS idx_posts_user_id ON posts (user_id);
CREATE INDEX IF NOT EXISTS idx_posts_forum_id ON posts (forum_id);
CREATE INDEX IF NOT EXISTS idx_posts_thread_id ON posts (thread_id);
CREATE INDEX IF NOT EXISTS idx_posts_created ON posts (created);
CREATE INDEX IF NOT EXISTS idx_posts_path1 ON posts ((__path[1]));
CREATE INDEX IF NOT EXISTS idx_posts_parents ON posts(id, parent_id, thread_id);
CREATE INDEX IF NOT EXISTS idx_posts_getbyid ON posts (id, forum_id);
CREATE INDEX IF NOT EXISTS idx_posts_flat ON posts (thread_id, created, id);
CREATE INDEX IF NOT EXISTS idx_posts_tree ON posts (thread_id, __path);
-- ============================================================================


CREATE TABLE IF NOT EXISTS forum_users (
  user_id INT REFERENCES users(id) NOT NULL,
  forum_id INT REFERENCES forums(id) NOT NULL
);
CREATE INDEX IF NOT EXISTS idx_forum_users_ui_user ON forum_users (user_id);
CREATE INDEX IF NOT EXISTS idx_forum_users_fi_forum ON forum_users (forum_id);


CREATE OR REPLACE FUNCTION add_forum_users() RETURNS TRIGGER AS '
  BEGIN
    INSERT INTO forum_users (user_id, forum_id) VALUES (NEW.user_id, NEW.forum_id);
    RETURN NEW;
  END;
' LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS post_insert_trigger ON posts;
CREATE TRIGGER post_insert_trigger AFTER INSERT ON posts
FOR EACH ROW EXECUTE PROCEDURE add_forum_users();

DROP TRIGGER IF EXISTS thread_insert_trigger ON threads;
CREATE TRIGGER thread_insert_trigger AFTER INSERT ON threads
FOR EACH ROW EXECUTE PROCEDURE add_forum_users();
