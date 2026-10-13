"""Generate the archaeology compass sprites, the per-frame models and the previews.

Authored at **16x16** so the compass shares the pixel density of the vanilla
compass and of other modded compasses. A 32x32 sprite reads as a different
fidelity class next to them, and gets crushed when the GUI draws it at
16 px; at 16x16 the needle survives at every GUI scale.

The copper rim uses a consistent upper-left light source, with a recessed
teal dial and a high-contrast gold needle. Two quiet side marks and a small
brass pivot retain the instrument identity without crowding the dial.
"""

from pathlib import Path
import math
import json

from PIL import Image, ImageChops, ImageDraw


ROOT = Path(__file__).resolve().parent
ASSETS = ROOT / "src/main/resources/assets/archaeologycompass"
TEXTURES = ASSETS / "textures/item"
MODELS = ASSETS / "models/item"
SIZE = 16
CENTER = (7.5, 7.0)
PERSPECTIVE = 0.62
TAIL_LENGTH = 2.2
SHELL_DEPTH = 2
TOP_FACE = (0, 2, 15, 12)
PALETTE = {
    "outline": "#30251E",
    "copper": "#BC7A47",
    "ring_light": "#FFD89B",
    "ring_dark": "#92532E",
    "dial": "#304B43",
    "dial_shadow": "#1D302D",
    "dial_light": "#3B5A50",
    "dial_mark": "#718779",
    "bevel_light": "#DEAA6A",
    "bevel_dark": "#A36B3E",
    "needle": "#FFF6D5",
    "needle_shadow": "#E5C17D",
    "tail": "#80DFC2",
    "tail_shadow": "#4DA78E",
    "pivot": "#B58349",
}


def top_face_mask():
    """The upper ellipse, used to find where the shell starts in each column."""
    mask = Image.new("L", (SIZE, SIZE))
    ImageDraw.Draw(mask).ellipse(TOP_FACE, fill=255)
    return mask


def shape_mask():
    """Silhouette of the whole body: top face plus the side-wall rows."""
    mask = Image.new("L", (SIZE, SIZE))
    draw = ImageDraw.Draw(mask)
    for offset in range(SHELL_DEPTH):
        draw.ellipse((TOP_FACE[0], TOP_FACE[1] + offset,
                      TOP_FACE[2], TOP_FACE[3] + offset), fill=255)
    return mask


def base_image():
    image = Image.new("RGBA", (SIZE, SIZE))
    draw = ImageDraw.Draw(image)
    top = top_face_mask()

    for offset in range(SHELL_DEPTH - 1, -1, -1):
        draw.ellipse((TOP_FACE[0], TOP_FACE[1] + offset,
                      TOP_FACE[2], TOP_FACE[3] + offset), fill=PALETTE["outline"])
    for x in range(1, 15):
        column = [y for y in range(SIZE) if top.getpixel((x, y))]
        if not column:
            continue
        bottom = max(column)
        shade = "#BA804E" if x < 6 else "#925D37" if x < 11 else "#68422C"
        draw.point((x, bottom + 1), fill=shade)

    draw.ellipse(TOP_FACE, fill=PALETTE["outline"])
    draw.ellipse((1, 3, 14, 11), fill=PALETTE["copper"])
    draw.arc((1, 3, 14, 11), 185, 305, fill=PALETTE["ring_light"])
    draw.arc((1, 3, 14, 11), 15, 155, fill=PALETTE["ring_dark"])
    draw.line((5, 3, 10, 3), fill=PALETTE["ring_light"])
    draw.point((2, 5), fill=PALETTE["ring_light"])

    for x in range(1, 15):
        column = [y for y in range(SIZE) if top.getpixel((x, y))]
        if not column:
            continue
        bottom = max(column)
        if bottom >= 9:
            draw.point((x, bottom),
                       fill=PALETTE["bevel_light"] if x < 8 else PALETTE["bevel_dark"])

    draw.ellipse((2, 4, 13, 10), fill=PALETTE["dial_shadow"])
    draw.ellipse((3, 5, 12, 9), fill=PALETTE["dial"])
    draw.line((5, 5, 9, 5), fill=PALETTE["dial_light"])
    draw.point((3, 7), fill=PALETTE["dial_mark"])
    draw.point((12, 7), fill=PALETTE["dial_mark"])
    return image


def frame_image(index, body, mask):
    image = body.copy()
    angle = index * math.tau / 32
    direction = (-math.sin(angle), math.cos(angle))
    perpendicular = (-direction[1], direction[0])
    front = []
    back = []
    for y in range(SIZE):
        for x in range(SIZE):
            dx = x - CENTER[0]
            dy = (y - CENTER[1]) / PERSPECTIVE
            along = dx * direction[0] + dy * direction[1]
            across = dx * perpendicular[0] + dy * perpendicular[1]
            if 0 <= along <= 4.2 and abs(across) <= max(0.45, 1.25 * (1 - along / 4.6)):
                front.append((x, y, across))
            elif -TAIL_LENGTH <= along < 0 and abs(across) <= max(0.4, 0.95 * (1 + along / (TAIL_LENGTH + 0.5))):
                back.append((x, y, across))

    draw = ImageDraw.Draw(image)
    tip = (round(CENTER[0] + direction[0] * 4.2),
           round(CENTER[1] + direction[1] * 4.2 * PERSPECTIVE))
    tail = (round(CENTER[0] - direction[0] * TAIL_LENGTH),
            round(CENTER[1] - direction[1] * TAIL_LENGTH * PERSPECTIVE))
    pivot = (round(CENTER[0]), round(CENTER[1]))
    draw.line((tail, pivot), fill=PALETTE["tail_shadow"])
    draw.line((pivot, tip), fill=PALETTE["needle_shadow"])
    for x, y, across in back:
        draw.point((x, y), fill=PALETTE["tail"] if across <= 0.25 else PALETTE["tail_shadow"])
    for x, y, across in front:
        draw.point((x, y), fill=PALETTE["needle"] if across <= 0 else PALETTE["needle_shadow"])
    draw.point((7, 7), fill=PALETTE["pivot"])
    draw.point((8, 7), fill=PALETTE["ring_light"])

    image.putalpha(ImageChops.multiply(image.getchannel("A"), mask))
    return image


def main():
    TEXTURES.mkdir(parents=True, exist_ok=True)
    body = base_image()
    mask = shape_mask()
    frames = [frame_image(index, body, mask) for index in range(32)]
    assert len({image.tobytes() for image in frames}) == 32
    assert all(image.size == (16, 16) and image.mode == "RGBA" for image in frames)
    assert all(image.getchannel("A").tobytes() == mask.tobytes() for image in frames)
    for index, image in enumerate(frames):
        name = f"archaeology_compass_{index:02d}"
        image.save(TEXTURES / f"{name}.png", optimize=True)
        model = {
            "parent": "minecraft:item/generated",
            "textures": {"layer0": f"archaeologycompass:item/{name}"},
        }
        (MODELS / f"{name}.json").write_text(json.dumps(model, indent=2) + "\n", encoding="utf-8")

    scale = 8
    preview = Image.new("RGB", (8 * (SIZE * scale + 16), 4 * (SIZE * scale + 32)), "#202725")
    draw = ImageDraw.Draw(preview)
    for index, image in enumerate(frames):
        x = (index % 8) * (SIZE * scale + 16) + 8
        y = (index // 8) * (SIZE * scale + 32) + 8
        scaled = image.resize((SIZE * scale, SIZE * scale), Image.Resampling.NEAREST)
        preview.paste(scaled, (x, y), scaled)
        draw.text((x + SIZE * scale // 2 - 6, y + SIZE * scale + 6), f"{index:02d}", fill="#E8DABD")
    preview.save(ROOT / "docs/archaeology_compass_preview.png")

    animation = []
    for index in range(32):
        canvas = Image.new("RGB", (256, 256), "#202725")
        scaled = frames[(index + 16) % 32].resize((256, 256), Image.Resampling.NEAREST)
        canvas.paste(scaled, (0, 0), scaled)
        animation.append(canvas)
    animation[0].save(
        ROOT / "docs/archaeology_compass_rotation.gif",
        save_all=True,
        append_images=animation[1:],
        duration=100,
        loop=0,
        disposal=2,
    )
    print(f"Generated 32 distinct {SIZE}x{SIZE} RGBA textures, 32 models, contact sheet and rotation preview.")


if __name__ == "__main__":
    main()
