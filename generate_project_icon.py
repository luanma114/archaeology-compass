"""Generate the project icon and the in-game mod list logo from the live V2 compass artwork.

The project icon (400x400) is uploaded to CurseForge as the project avatar.
The mod list logo (128x128) ships inside the JAR and is referenced by
`logoFile` in neoforge.mods.toml.

Requires Python 3 and Pillow. This script is MIT-licensed; the generated
artwork follows the project's CC BY 4.0 artwork license.
"""

from pathlib import Path
import math

from PIL import Image, ImageDraw


ROOT = Path(__file__).resolve().parent
SOURCE = ROOT / "src/main/resources/assets/archaeologycompass/textures/item/archaeology_compass_19.png"
OUTPUT = ROOT / "docs/archaeology_compass_icon_400.png"
SIZE = 400
# In-game mod list logo: the compass alone, without the project-page frame.
LOGO_OUTPUT = ROOT / "src/main/resources/archaeology_compass_logo.png"
LOGO_SIZE = 128


def load_sprite():
    """Load the V2 compass frame, cropped to its non-transparent bounds."""
    with Image.open(SOURCE) as source:
        sprite = source.convert("RGBA")
    bounds = sprite.getbbox()
    if bounds is None:
        raise ValueError("The compass source texture is empty.")
    return sprite.crop(bounds)


def build_logo():
    """Render the in-game mod list logo from the same artwork.

    The logo is the compass alone — no project-page rings or corner marks —
    so it stays readable at the small size the mod list draws it. Integer
    scaling keeps pixel-art edges exact, and the transparent background lets
    the mod list draw its own backdrop behind it.
    """
    sprite = load_sprite()
    scale = max(1, LOGO_SIZE // max(sprite.width, sprite.height))
    sprite = sprite.resize(
        (sprite.width * scale, sprite.height * scale), Image.Resampling.NEAREST
    )
    logo = Image.new("RGBA", (LOGO_SIZE, LOGO_SIZE), (0, 0, 0, 0))
    logo.alpha_composite(
        sprite,
        ((LOGO_SIZE - sprite.width) // 2, (LOGO_SIZE - sprite.height) // 2),
    )
    logo.save(LOGO_OUTPUT, optimize=True)

    with Image.open(LOGO_OUTPUT) as saved:
        assert saved.size == (LOGO_SIZE, LOGO_SIZE)
        assert saved.format == "PNG"
        print(
            f"Created {LOGO_OUTPUT.name}: {saved.size[0]}x{saved.size[1]}, "
            f"{saved.mode}, {LOGO_OUTPUT.stat().st_size} bytes, sprite scale {scale}x"
        )


def main():
    # Render the backdrop on a 100-pixel grid to retain a pixel-art feel.
    backdrop = Image.new("RGB", (100, 100))
    for y in range(100):
        for x in range(100):
            distance = math.hypot((x - 49.5) / 70, (y - 45) / 70)
            light = max(0.0, 1.0 - distance) ** 1.4
            backdrop.putpixel((x, y), (
                round(20 + 18 * light),
                round(28 + 22 * light),
                round(27 + 20 * light),
            ))

    draw = ImageDraw.Draw(backdrop)
    # A quiet circular survey motif frames the compass without tiny text.
    draw.ellipse((8, 8, 91, 91), outline="#354440", width=1)
    draw.ellipse((12, 12, 87, 87), outline="#2D3C37", width=1)
    for index in range(16):
        angle = index * math.tau / 16
        outer = (round(49.5 + math.sin(angle) * 41),
                 round(49.5 - math.cos(angle) * 41))
        inner_radius = 36 if index % 4 == 0 else 39
        inner = (round(49.5 + math.sin(angle) * inner_radius),
                 round(49.5 - math.cos(angle) * inner_radius))
        draw.line((inner, outer), fill="#78684B" if index % 4 == 0 else "#435149")

    # Copper corner marks remain legible in small project listings.
    for corner_x, corner_y, sign_x, sign_y in (
        (7, 7, 1, 1), (92, 7, -1, 1),
        (7, 92, 1, -1), (92, 92, -1, -1),
    ):
        draw.line((corner_x, corner_y, corner_x + 8 * sign_x, corner_y), fill="#896140")
        draw.line((corner_x, corner_y, corner_x, corner_y + 8 * sign_y), fill="#896140")
        draw.point((corner_x, corner_y), fill="#C58B58")

    icon = backdrop.resize((SIZE, SIZE), Image.Resampling.NEAREST).convert("RGBA")
    # Integer scaling preserves the exact colors and shapes of the game art.
    sprite = load_sprite()
    sprite = sprite.resize((sprite.width * 10, sprite.height * 10), Image.Resampling.NEAREST)
    x = (SIZE - sprite.width) // 2
    y = (SIZE - sprite.height) // 2 - 4

    shadow = Image.new("RGBA", icon.size)
    shadow_color = Image.new("RGBA", sprite.size, (6, 11, 10, 145))
    shadow_color.putalpha(sprite.getchannel("A").point(lambda alpha: round(alpha * 0.57)))
    shadow.alpha_composite(shadow_color, (x + 8, y + 12))
    icon = Image.alpha_composite(icon, shadow)
    icon.alpha_composite(sprite, (x, y))
    # Opaque PNG avoids website background differences around the artwork.
    icon.convert("RGB").save(OUTPUT, optimize=True)

    with Image.open(OUTPUT) as saved:
        assert saved.size == (400, 400)
        assert saved.format == "PNG"
        print(f"Created {OUTPUT.name}: {saved.size[0]}x{saved.size[1]}, {saved.mode}, {OUTPUT.stat().st_size} bytes")

    build_logo()


if __name__ == "__main__":
    main()
