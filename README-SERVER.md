# For server owners and operators

## Configuration
Configuration is read from ./config/pechkin.json5

Pechkin exposes the following configurable parameters:

```json5
{
    // The maximum amount of messages a player's inbox can have.
    maxInboxMessages: 100,

    // The amount of players that will be stored in the player's correspondents list.
    // Used for command suggestions.
    maxCorrespondents: 10,

    // [Rate limit tuning]
    // Pechkin uses a rate limiting algorithm (equivalent to a token bucket) to prevent players from spamming.
    // Each action costs tokens, and each player has an allowance of tokens that grows over time (the bucket)
    // This prevents players from abusing the system while still allowing short bursts of activity.
    
    // Pechkin fixes a fill rate of one token per second, so we will refer to these tokens as "seconds" from now on 
        
    // Bucket capacity, seconds
    maxTimeDebt: 120,
    // Cost of sending a single message, in seconds
    sendCost: 30
}
```
