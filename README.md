# Roleplay Chat

This mod allows players to communicate in a roleplay (RP) setting with different ranges for various types of messages. The goal is to enhance RP immersion by limiting how far messages can be seen based on their type.

## How It Works

Messages are triggered by a special character placed at the start of your message. For example, `!Hello` will shout "Hello" (range of 80 blocks).

When speaking in the chat, **your message will NOT be visible across the entire server**. If no player is within your message’s range, you won’t get a response! Check the table below to understand how the system works.

This approach is designed to foster RP and local interactions between players.

| **TYPE**  | **COLOR**        | **RANGE**  | **TRIGGER CHARACTER** | **TRIGGER COMMAND**   | **DESCRIPTION**       |
|-----------|------------------|------------|-----------------------|-----------------------|-----------------------|
| Whisper   | 🟪 Violet (#C3C) | 4 blocks   | `«` or `"`            | `/whisper <message>`  | Speak quietly to someone nearby             |
| Action    | 🟩 Green (#3C3)  | 25 blocks  | `*`                   | `/action <message>`   | Describe an action you're performing (like `/me`) |
| Speak     | ⬜️ White (#FFF)  | 30 blocks  | None                  | `/speak <message>`    | Normal conversation                         |
| Shout     | 🟥 Red (#C30)    | 80 blocks  | `!`                   | `/shout <message>`    | Shout, for example, before attacking!       |

## Additional Commands (Optional)
These commands are for **Out of Character (OOC)** interactions. Use them sparingly! Thank you.

| **TYPE**             | **COLOR**       | **RANGE** | **TRIGGER CHARACTER** | **TRIGGER COMMAND**     | **DESCRIPTION**            |
|----------------------|-----------------|-----------|-----------------------|-------------------------|----------------------------|
| Support              | Pink (#F9CF9C)     | Unlimited | `?`                   | `/support <message>`    | Request help from staff (admins/helpers)            |
| Private Message (PM) | White (#FFFFFF) | Unlimited | none                  | `/m <player> <message>` | Send a private message to another player            |
| Reply to PM          | White (#FFFFFF) | Unlimited | none                  | `/r <message>`          | Reply to the last received private message          |
| OOC Chat             | Gray (#AEC1D5)  | 60 blocks | `(`                   | `/ooc <message>`        | Chat out of character on the server                 |
| Global OOC           | Gray (#AEC1D5)  | Unlimited | `[`                   | `/globalOoc <message>`  | Speak in a global OOC channel (requires activation) |

### Global OOC Chat
To speak in the global OOC chat, you first need to activate it by using the `/global` command. **By default, it is activated**. You can deactivate it with the same command, `/global`.

Once activated, this command allows you to communicate with all players who have also activated the global OOC chat. Keep in mind that not all players use the global chat, so you may not always get responses.
