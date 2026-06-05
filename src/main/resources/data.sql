-- Inserts para Estádios (Stadiums)
INSERT INTO stadium (id, name, city, state, active) VALUES ('10000000-0000-0000-0000-000000000001', 'Neo Química Arena', 'São Paulo', 'SP', true);
INSERT INTO stadium (id, name, city, state, active) VALUES ('10000000-0000-0000-0000-000000000002', 'Maracanã', 'Rio de Janeiro', 'RJ', true);
INSERT INTO stadium (id, name, city, state, active) VALUES ('10000000-0000-0000-0000-000000000003', 'Mineirão', 'Belo Horizonte', 'MG', true);

-- Inserts para Times (Clubs)
INSERT INTO club (id, name, state, foundation_date, active) VALUES ('20000000-0000-0000-0000-000000000001', 'Corinthians', 'SP', '1910-09-01', true);
INSERT INTO club (id, name, state, foundation_date, active) VALUES ('20000000-0000-0000-0000-000000000002', 'Palmeiras', 'SP', '1914-08-26', true);
INSERT INTO club (id, name, state, foundation_date, active) VALUES ('20000000-0000-0000-0000-000000000003', 'São Paulo', 'SP', '1930-01-25', true);
INSERT INTO club (id, name, state, foundation_date, active) VALUES ('20000000-0000-0000-0000-000000000004', 'Flamengo', 'RJ', '1895-11-17', true);
INSERT INTO club (id, name, state, foundation_date, active) VALUES ('20000000-0000-0000-0000-000000000005', 'Vasco da Gama', 'RJ', '1898-08-21', true);
INSERT INTO club (id, name, state, foundation_date, active) VALUES ('20000000-0000-0000-0000-000000000006', 'Atlético Mineiro', 'MG', '1908-03-25', true);
INSERT INTO club (id, name, state, foundation_date, active) VALUES ('20000000-0000-0000-0000-000000000007', 'Cruzeiro', 'MG', '1921-01-02', true);

-- Inserts para Partidas (Matches)
INSERT INTO match (id, home_club_id, away_club_id, stadium_id, home_club_goals, away_club_goals, match_date_time)
VALUES ('30000000-0000-0000-0000-000000000001', '20000000-0000-0000-0000-000000000001', '20000000-0000-0000-0000-000000000002', '10000000-0000-0000-0000-000000000001', 7, 1, '2024-07-01T16:00:00');
INSERT INTO match (id, home_club_id, away_club_id, stadium_id, home_club_goals, away_club_goals, match_date_time)
VALUES ('30000000-0000-0000-0000-000000000002', '20000000-0000-0000-0000-000000000003', '20000000-0000-0000-0000-000000000004', '10000000-0000-0000-0000-000000000002', 1, 3, '2024-07-02T18:00:00');
INSERT INTO match (id, home_club_id, away_club_id, stadium_id, home_club_goals, away_club_goals, match_date_time)
VALUES ('30000000-0000-0000-0000-000000000003', '20000000-0000-0000-0000-000000000005', '20000000-0000-0000-0000-000000000006', '10000000-0000-0000-0000-000000000002', 0, 0, '2024-07-03T20:00:00');
INSERT INTO match (id, home_club_id, away_club_id, stadium_id, home_club_goals, away_club_goals, match_date_time)
VALUES ('30000000-0000-0000-0000-000000000004', '20000000-0000-0000-0000-000000000007', '20000000-0000-0000-0000-000000000001', '10000000-0000-0000-0000-000000000003', 3, 2, '2024-07-04T15:00:00');
INSERT INTO match (id, home_club_id, away_club_id, stadium_id, home_club_goals, away_club_goals, match_date_time)
VALUES ('30000000-0000-0000-0000-000000000005', '20000000-0000-0000-0000-000000000001', '20000000-0000-0000-0000-000000000003', '10000000-0000-0000-0000-000000000001', 0, 2, '2024-07-05T17:00:00');
INSERT INTO match (id, home_club_id, away_club_id, stadium_id, home_club_goals, away_club_goals, match_date_time)
VALUES ('30000000-0000-0000-0000-000000000006', '20000000-0000-0000-0000-000000000002', '20000000-0000-0000-0000-000000000004', '10000000-0000-0000-0000-000000000002', 1, 1, '2024-07-06T19:00:00');
INSERT INTO match (id, home_club_id, away_club_id, stadium_id, home_club_goals, away_club_goals, match_date_time)
VALUES ('30000000-0000-0000-0000-000000000007', '20000000-0000-0000-0000-000000000005', '20000000-0000-0000-0000-000000000007', '10000000-0000-0000-0000-000000000003', 2, 2, '2024-07-07T14:00:00');
INSERT INTO match (id, home_club_id, away_club_id, stadium_id, home_club_goals, away_club_goals, match_date_time)
VALUES ('30000000-0000-0000-0000-000000000008', '20000000-0000-0000-0000-000000000006', '20000000-0000-0000-0000-000000000002', '10000000-0000-0000-0000-000000000003', 1, 0, '2024-07-08T16:00:00');
INSERT INTO match (id, home_club_id, away_club_id, stadium_id, home_club_goals, away_club_goals, match_date_time)
VALUES ('30000000-0000-0000-0000-000000000009', '20000000-0000-0000-0000-000000000001', '20000000-0000-0000-0000-000000000004', '10000000-0000-0000-0000-000000000001', 2, 2, '2024-07-09T18:00:00');
INSERT INTO match (id, home_club_id, away_club_id, stadium_id, home_club_goals, away_club_goals, match_date_time)
VALUES ('30000000-0000-0000-0000-000000000010', '20000000-0000-0000-0000-000000000002', '20000000-0000-0000-0000-000000000005', '10000000-0000-0000-0000-000000000002', 0, 1, '2024-07-10T20:00:00');
INSERT INTO match (id, home_club_id, away_club_id, stadium_id, home_club_goals, away_club_goals, match_date_time)
VALUES ('30000000-0000-0000-0000-000000000011', '20000000-0000-0000-0000-000000000003', '20000000-0000-0000-0000-000000000006', '10000000-0000-0000-0000-000000000003', 1, 0, '2024-07-11T15:00:00');
