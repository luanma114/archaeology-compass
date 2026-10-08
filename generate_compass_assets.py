from pathlib import Path
import math
import json

from PIL import Image, ImageDraw


ROOT = Path(__file__).resolve().parent
ASSETS = ROOT / "src/main/resources/assets/archaeologycompass"
TEXTURES = ASSETS / "textures/item"
MODELS = ASSETS / "models/item"
SIZE = 32
CENTER = (15.5, 15.5)
PERSPECTIVE = 0.64
PALETTE = {
    "outline": "#352821",
    "copper_light": "#F2BB7A",
    "copper": "#C7814D",
    "copper_shadow": "#865035",
    "patina": "#548778",
    "dial": "#343C39",
    "dial_shadow": "#252D2C",
    "tick": "#A9946A",
    "needle": "#F6E5B0",
    "needle_shadow": "#C8AD73",
    "tail": "#68AD9A",
}


def base_image():
    image = Image.new("RGBA", (SIZE, SIZE))
    draw = ImageDraw.Draw(image)
    draw.ellipse((1, 6, 30, 26), fill=PALETTE["outline"])
    draw.ellipse((2, 7, 29, 25), fill=PALETTE["copper_shadow"])
    draw.ellipse((1, 5, 30, 25), fill=PALETTE["outline"])
    draw.ellipse((2, 6, 29, 24), fill=PALETTE["copper"])
    draw.arc((2, 6, 29, 24), 185, 310, fill=PALETTE["copper_light"], width=2)
    draw.arc((2, 6, 29, 24), 10, 160, fill=PALETTE["copper_shadow"])
    draw.ellipse((4, 7, 27, 23), fill=PALETTE["outline"])
    draw.ellipse((5, 8, 26, 22), fill=PALETTE["dial_shadow"])
    draw.ellipse((6, 9, 25, 22), fill=PALETTE["dial"])
    for index in range(8):
        angle = index * math.tau / 8
        outer = (round(CENTER[0] + math.sin(angle) * 10),
                 round(CENTER[1] - math.cos(angle) * 10 * PERSPECTIVE))
        inner = (round(CENTER[0] + math.sin(angle) * 8.5),
                 round(CENTER[1] - math.cos(angle) * 8.5 * PERSPECTIVE))
        draw.line((inner, outer), fill=PALETTE["tick"])
    for point in ((4, 19), (5, 20), (25, 20), (26, 19)):
        draw.point(point, fill=PALETTE["patina"])
    return image


def frame_image(index):
    image = base_image()
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
            if 0 <= along <= 9.0 and abs(across) <= max(0.5, 2.5 * (1 - along / 9.7)):
                front.append((x, y, across))
            elif -6.8 <= along < 0 and abs(across) <= max(0.45, 1.8 * (1 + along / 7.5)):
                back.append((x, y))
    draw = ImageDraw.Draw(image)
    needle_pixels = {(x, y) for x, y, _ in front} | set(back)
    for x, y in needle_pixels:
        for offset_x, offset_y in ((-1, 0), (1, 0), (0, -1), (0, 1)):
            if (x + offset_x, y + offset_y) not in needle_pixels:
                draw.point((x + offset_x, y + offset_y), fill=PALETTE["dial_shadow"])
    for x, y in back:
        draw.point((x, y), fill=PALETTE["tail"])
    for x, y, across in front:
        draw.point((x, y), fill=PALETTE["needle"] if across <= 0 else PALETTE["needle_shadow"])
    draw.rectangle((14, 15, 17, 16), fill=PALETTE["outline"])
    draw.line((15, 15, 16, 15), fill=PALETTE["copper_light"])
    return image


def main():
    TEXTURES.mkdir(parents=True, exist_ok=True)
    frames = [frame_image(index) for index in range(32)]
    for index, image in enumerate(frames):
        name = f"archaeology_compass_{index:02d}"
        image.save(TEXTURES / f"{name}.png")
        model = {
            "parent": "minecraft:item/generated",
            "textures": {"layer0": f"archaeologycompass:item/{name}"},
        }
        (MODELS / f"{name}.json").write_text(json.dumps(model, indent=2) + "\n", encoding="utf-8")

    preview = Image.new("RGB", (8 * 144, 4 * 160), "#202725")
    draw = ImageDraw.Draw(preview)
    for index, image in enumerate(frames):
        x = (index % 8) * 144 + 8
        y = (index // 8) * 160 + 8
        scaled = image.resize((128, 128), Image.Resampling.NEAREST)
        preview.paste(scaled, (x, y), scaled)
        draw.text((x + 54, y + 136), f"{index:02d}", fill="#E8DABD")
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
    assert len({image.tobytes() for image in frames}) == 32
    print("Generated 32 distinct RGBA textures, 32 models, contact sheet and rotation preview.")


if __name__ == "__main__":
    main()
