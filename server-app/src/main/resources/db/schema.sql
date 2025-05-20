DROP TABLE IF EXISTS BET;
CREATE TABLE bet (
  id BIGINT PRIMARY KEY,
  user_id BIGINT,
  event_id BIGINT,
  event_market_id BIGINT,
  event_winner_id BIGINT,
  bet_amount DECIMAL(10, 2)
);
