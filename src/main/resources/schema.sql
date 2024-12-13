DROP TABLE IF EXISTS film_genres;
DROP TABLE IF EXISTS friends;
DROP TABLE IF EXISTS likes;
DROP TABLE IF EXISTS genres;
DROP TABLE IF EXISTS users CASCADE;
DROP TABLE IF EXISTS films;
DROP TABLE IF EXISTS rating_mpa;
DROP TABLE IF EXISTS film_directors;
DROP TABLE IF EXISTS directors;

CREATE TABLE IF NOT EXISTS genres (
    genre_id INT NOT NULL GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    genre_name VARCHAR(255) NOT NULL
);

CREATE TABLE IF NOT EXISTS rating_mpa (
    rating_id INT NOT NULL GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    rating_name VARCHAR(255) NOT NULL
);

CREATE TABLE IF NOT EXISTS films (
    film_id INT NOT NULL GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    film_name VARCHAR(255) NOT NULL,
    description VARCHAR(200) NOT NULL,
    duration INT NOT NULL CHECK (duration > 0),
    release_date TIMESTAMP NOT NULL,
    rating_id INT DEFAULT 1 REFERENCES rating_mpa(rating_id)
        ON DELETE RESTRICT ON UPDATE CASCADE
);

CREATE TABLE IF NOT EXISTS directors (
    id BIGINT NOT NULL GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    name VARCHAR(50) NOT NULL
);

CREATE TABLE IF NOT EXISTS film_directors (
    film_id INT NOT NULL,
    director_id BIGINT NOT NULL,
    CONSTRAINT fk_film_directors FOREIGN KEY (film_id) REFERENCES films (film_id) ON DELETE CASCADE,
    CONSTRAINT fk_director FOREIGN KEY (director_id) REFERENCES directors (id) ON DELETE CASCADE,
    CONSTRAINT unique_film_director UNIQUE (film_id, director_id)
);

CREATE TABLE IF NOT EXISTS users (
    user_id INT NOT NULL GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    user_name VARCHAR(255),
    login VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL,
    birthday DATE
);

CREATE TABLE IF NOT EXISTS film_genres (
    film_id INT NOT NULL REFERENCES films (film_id) ON DELETE RESTRICT ON UPDATE CASCADE,
    genre_id INT NOT NULL REFERENCES genres (genre_id) ON DELETE RESTRICT ON UPDATE CASCADE
);

CREATE TABLE IF NOT EXISTS likes (
    film_id INT NOT NULL REFERENCES films (film_id),
    user_id INT NOT NULL REFERENCES users (user_id)
);

CREATE TABLE IF NOT EXISTS reviews (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    content VARCHAR(400) NOT NULL,
    is_positive BOOLEAN NOT NULL,
    user_id INT NOT NULL,
    film_id INT NOT NULL,
    CONSTRAINT fk_film_reviews FOREIGN KEY (film_id) REFERENCES films (film_id) ON DELETE CASCADE,
    CONSTRAINT fk_user_reviews FOREIGN KEY (user_id) REFERENCES users (user_id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS review_ratings (
    review_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    is_like BOOLEAN NOT NULL,
    CONSTRAINT fk_review FOREIGN KEY (review_id) REFERENCES reviews (id) ON DELETE CASCADE,
    CONSTRAINT fk_user_review_ratings FOREIGN KEY (user_id) REFERENCES users (user_id) ON DELETE CASCADE,
    CONSTRAINT unique_review__ratings_user UNIQUE (review_id, user_id)
);

ALTER TABLE likes ADD CONSTRAINT unique_like UNIQUE (user_id, film_id);

CREATE TABLE IF NOT EXISTS friends (
    user_id INT NOT NULL REFERENCES users (user_id) ON DELETE RESTRICT ON UPDATE CASCADE,
    friend_id INT NOT NULL REFERENCES users (user_id) ON DELETE RESTRICT ON UPDATE CASCADE,
    status BOOLEAN NOT NULL
);

CREATE TABLE IF NOT EXISTS feed (
    event_id    INT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    user_id     INT,
    timestamp   BIGINT NOT NULL,
    event_type  VARCHAR(20) NOT NULL,
    operation    VARCHAR(20) NOT NULL,
    entity_id   INT NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users (user_id) ON DELETE CASCADE
);