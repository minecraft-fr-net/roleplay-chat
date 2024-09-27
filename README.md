# Roleplay Chat

This mod allows players to communicate in a roleplay (RP) setting with different ranges for various types of messages. The goal is to enhance RP immersion by limiting how far messages can be seen based on their type.

## How It Works

Messages are triggered by a special character placed at the start of your message. For example, `!Hello` will shout "Hello" (range of 80 blocks).

When speaking in the chat, **your message will NOT be visible across the entire server**. If no player is within your message’s range, you won’t get a response! Check the table below to understand how the system works.

This approach is designed to foster RP and local interactions between players.

| **TYPE**  | **COLOR** | **RANGE**  | **TRIGGER CHARACTER** | **DESCRIPTION**                             |
|-----------|-----------|------------|-----------------------|---------------------------------------------|
| Whisper   | Violet    | 4 blocks   | `«`                   | Speak quietly to someone nearby             |
| Action    | Green     | 25 blocks  | `*`                   | Describe an action you're performing (like `/me`) |
| Speak     | White     | 30 blocks  | None                  | Normal conversation                         |
| Shout     | Red       | 80 blocks  | `!`                   | Shout, for example, before attacking!       |

## Additional Commands (Optional)
These commands are for **Out of Character (OOC)** interactions. Use them sparingly! Thank you.

| **TYPE**      | **COLOR** | **RANGE**  | **TRIGGER CHARACTER** | **DESCRIPTION**                                    |
|---------------|-----------|------------|-----------------------|----------------------------------------------------|
| Support       | Pink      | Unlimited  | `?`                   | Request help from staff (admins/helpers)            |
| Private Message (PM) | White  | Unlimited | `/m username message` | Send a private message to another player           |
| Reply to PM   | White     | Unlimited  | `/r message`          | Reply to the last received private message          |
| OOC Chat      | Gray      | 60 blocks  | `(`                   | Chat out of character on the server                 |
| Global OOC    | Gray      | Unlimited  | `)`                   | Speak in a global OOC channel (requires activation) |

### Global OOC Chat
To speak in the global OOC chat, you first need to activate it by using the `/global` command. **By default, it is activated**. You can deactivate it with the same command, `/global`.

Once activated, this command allows you to communicate with all players who have also activated the global OOC chat. Keep in mind that not all players use the global chat, so you may not always get responses.
