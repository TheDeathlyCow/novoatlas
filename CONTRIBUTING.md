# NovoAtlas contribution guidelines

Thank you for your interest in contributing to NovoAtlas!

This documentation provides contribution guidelines for NovoAtlas. This can be updated at any time, so please check back
when working on any contribution even if you have contributed before.

I appreciate all contributions, but I prefer to discuss changes before you open a PR, especially for large changes
or new features. Large, unsolicited PRs **are likely to be rejected**.

Each PR should focus on just one thing, and the diff should be as small as possible. When you make a PR, clearly define
its scope in the description and keep all changes *strictly limited* to that scope. Your PR should not combine multiple
features/fixes or include changes from other unmerged PRs. The reason I ask this is it makes it easier for me to review
and understand your code; I am not going to merge something I do not understand. It also helps to better track changes
in the version control, since each PR will be squashed when merged.

## Bug Fixes

Please report any bugs to the issue tracker first, even if you intend to fix it right away with a pull request, unless
the fix is *very* small (like a 1 line change). This is to allow for the issue to still be documented if your PR does
not progress for whatever reason.

## New Features

If you intend to introduce new features to NovoAtlas, this *must* be discussed with me and agreed upon in one of
my [contact channels](#contact-me) first. Every feature added to NovoAtlas increases my maintenance burden, especially
with regard to game updates, so I may not always been super keen on adding new features that are not necessary.
Especially in regard to new features, please do not waste time on unsolicited work.

## Testing

NovoAtlas does not (currently) use any sort of automated testing, it instead relies upon manual testing. You can conduct
manual tests using the builtin example datapacks. These are builtin to the mod, and available to be added during world
creation. By default, the `avila-basic-example` will be activated in the development environment, but it can be replaced
with any of the others.

**PRs must be tested on both Fabric and NeoForge,** and your PR description should clearly explain how you conducted
your tests so that I can replicate them.

## Code Style and Practices

Generally, I try to follow the style guidelines of
[Fabric API](https://github.com/FabricMC/fabric/blob/HEAD/CONTRIBUTING.md). However, this is not strictly enforced
with a style checker and some leeway is allowed (especially as noted below). If you have any questions, feel free
to [ask me](#contact-me).

Some specific style guidelines in addition to the Fabric API guidelines:

- Use the Official Mappings ("Mojmap")
- Do not use `api` or `impl` packages
- The Java API is *not* stable, breaking changes are allowed anywhere.
- Precondition assertions are not necessary.
- The Data Pack / JSON API *is* stable, breaking changes should be avoided as much as possible.
- `Optional`s are ok for use in parameters, if they are required for a codec.
- Record classes are preferred for most codec types.

### Strict Rules

- **Do not "vibe-code":** If you do not understand your code, then I assume that you have not tested it well. LLMs
  generally do not work well with large production code bases like Minecraft anyway. They may mix up game versions, use
  incorrect mappings, or make false assumptions about how the game is actually structured.
- **Do not add new external libraries:** If you think one is needed, please [ask me about it first](#contact-me). I will
  be the one to evaluate and integrate it. Remember that Minecraft already provides a large number of common Java
  libraries already, you may not even need to add it in the first place!
- **You may reuse code from other projects,** however you may only do so as long as that code's license permits it, and
  it is clearly cited in a comment.

## Contact Me

These are the best places to contact me regarding questions about NovoAtlas:

- [NovoAtlas Issues](https://github.com/TheDeathlyCow/novoatlas/issues)
- [Discord](https://discord.thedeathlycow.com)
- You can also make a "draft" pull request and ask questions in the comments/description.

