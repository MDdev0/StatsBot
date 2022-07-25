# Discord Stats Pinger
Get pings in your discord servers whenever statistics about someone on the internet changes!

**PREFIX:** `$`

## Setup
1. Add the bot to your server.
2. Create a channel called `#stat-pings`.
	- Commands can be run in any channel, but all pings will appear in `#stat-pings`.
	- The bot will try to find a suitable channel if no dedicated channel is found.
3. Add sources and watch the pings roll in.

## Commands
- Watch<br>
	Adds a source to your server to be notified about.<br>
	`$watch` `<link to source>` `<source type>`<p>

- Unwatch<br>
	Removes a source from your server and stop notifying you.<br>
	`$unwatch` `<link to source>` `<source type>`<p>

- Bug<br>
	Report a bug.<br>
	`$bug`<p>

- Help<br>
	Get help on the bot commands.<br>
	`$help`<p>

- More commands coming soon

## Supported Source Types
- YouTube
	- Channel Subscribers
- ~~Twitter~~
    - ~~User's Followers~~
- More types coming soon

*Twitter sources are currently non-functional due to an in-progress API changeover (RIP mixerno.space).*
*The commands for them still exist, but they are not at all supported currently (until I decide to deal with Twitter's API).*
<u>*Please remember that if a report already exists for your issue, you should just comment there instead of creating a new bug report.*