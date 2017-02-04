select accounts.username account, bots.username bot, server_id,channel,server_accepting_time,channel_created_time,joined_time,joined_place,(joined_time - channel_created_time)/1000000 as joined_after from channel_joins join users accounts on channel_joins.alias_id=accounts.id join users bots on channel_joins.user_id = bots.id where channel_joins.alias_id != channel_joins.user_id and channel_joins.alias_id is not null and (joined_time - channel_created_time) < 500000000 and joined_place > 1 and channel='dark' and server_id=2 order by channel;

SELECT
    accounts.username as account,
    bots.username as bot,
    channel,
    server_accepting_time,
    channel_created_time,
    joined_time,
    joined_place,
    (joined_time - channel_created_time) / 1000000 as joined_after_created

FROM channel_joins
JOIN users accounts
ON channel_joins.alias_id = accounts.id
JOIN users bots
ON channel_joins.user_id = bots.id

WHERE channel_joins.alias_id != channel_joins.user_id
AND channel_joins.alias_id is not null
AND (joined_time - channel_created_time) < 500000000
AND server_id = 2

ORDER BY channel;


-- avg joined place

SELECT
    channel,
    accounts.username as account,
    AVG(joined_time - channel_created_time) / 1000000 as avg_joined_time,
    AVG(joined_place) as avg_joined_place

FROM channel_joins
JOIN users accounts
ON channel_joins.alias_id = accounts.id

WHERE server_id = 2
AND channel_joins.alias_id != channel_joins.user_id
AND channel_joins.alias_id is not null
AND channel not in ('chat','init 6','the void')
AND (joined_time - channel_created_time) < 50000000

GROUP BY accounts.username, channel
ORDER BY channel;