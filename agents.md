Environment:
Minecraft Mod (Fabric 1.21.8)

Strict Rules:
- ALWAYS use perfectly crystal clear simple symmetrical names for entities that immediately convey their purpose, environment, and usage (e.g. auth-browser.ts / auth-server.ts, createBrowserClient / createServerClient)
- Try to use non-duplicate naming where it's best (e.g. having two middleware.ts files in different places seems weird)
- NEVER overcomplicate ANY single entity i.e. a asset/component/function/etc.
- NEVER add complicated logic to the design, ONLY consistent, predictable design with NO possible edge cases
- Propose to create REUSABLE entities i.e. components/functions/assets/etc. wherever you feel like something might be used multiple times somewhere in the project in the future
- NEVER silence errors
- NEVER ignore unused parameters, instead remove them wherever possible
- Do NOT leave dead code behind
- After switching patterns (e.g., config formats), search for and remove superseded files and references
- Before deleting anything potentially non-trivial (docs, large files, or anything unclear), ASK me first
- Always run a quick repo-wide grep for references before deleting
- Review agents.md often to make sure the Info section below is up to date and kept concise.

Info:
This is a production-ready Minecraft mod with existing users called Real Serene Seasons. It keeps Serene Seasons in sync with the real-world calendar. Please ensure consistent, clear, and easy to understand changes. Always talk to me about important considerations, backwards compat, and other key points.
