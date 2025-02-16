**Features**

- Mobs are now affected by regular climate calculations as normal. Their base temperature is calculated based on their
  spawn location.
- Mobs are more biased towards relatively temperate blocks when pathfinding.
- Added integration with `Serene Seasons` mod
- Prevented freeze-immune and fire-immune mobs from freezing or overheating, respectively
- Counted lit and redstone-powered blocks as giving off heat
- Decreased water temperatures and altitude-based temperature fluctuation

**Technical**

- Added (incomplete) user config system
- Improved the `/temperature` command
- Added `set_total` temperature modifier operation
- Tweaked entity tags
- Made mild optimizations
- Added jar manifest

**Bugfixes**

- Fixed missing translations
- Fixed some faulty conditions
- I'm not fixing the bug that armor stands can sweat and shiver. I can, but it's REALLY funny.

**Developer's Note**: Sorry for the major update so soon. Some of my planned features were larger than assumed, and I
had more motivation than I thought ^v^