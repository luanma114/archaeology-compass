from pathlib import Path
import math
import json

from PIL import Image, ImageDraw


ROOT = Path(__file__).resolve().parent
ASSETS = ROOT / "src/main/resources/assets/archaeologycompass"
TEXTURES = ASSETS / "textures/item"
MODELS = ASSETS / "models/item"
SIZE = 32
CENTER = (15.5, 17.0)
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
    draw.rounded_rectangle((11, 0, 20, 7), radius=3, fill=PALETTE["outline"])
    draw.rounded_rectangle((12, 1, 19, 6), radius=2, fill=PALETTE["copper"])
    draw.line((13, 1, 18, 1), fill=PALETTE["copper_light"])
    draw.rectangle((14, 3, 17, 5), fill=(0, 0, 0, 0))
    draw.rectangle((13, 6, 18, 8), fill=PALETTE["copper_shadow"])
    draw.ellipse((1, 4, 30, 30), fill=PALETTE["outline"])
    draw.ellipse((2, 5, 29, 29), fill=PALETTE["copper_shadow"])
    draw.ellipse((3, 5, 28, 28), fill=PALETTE["copper"])
    draw.arc((3, 5, 28, 28), 185, 300, fill=PALETTE["copper_light"], width=2)
    draw.arc((3, 6, 28, 28), 15, 100, fill=PALETTE["copper_shadow"], width=2)
    draw.ellipse((5, 7, 26, 26), fill=PALETTE["outline"])
    draw.ellipse((6, 8, 25, 25), fill=PALETTE["tick"])
    draw.ellipse((7, 9, 24, 24), fill=PALETTE["dial_shadow"])
    draw.ellipse((8, 10, 23, 23), fill=PALETTE["dial"])
    draw.arc((8, 10, 23, 23), 20, 150, fill=PALETTE["dial_shadow"])
    for index in range(16):
        angle = index * math.tau / 16
        outer = (round(CENTER[0] + math.sin(angle) * 8.5), round(CENTER[1] - math.cos(angle) * 7.5))
        inner = (round(CENTER[0] + math.sin(angle) * (6.5 if index % 4 == 0 else 7.5)),
                 round(CENTER[1] - math.cos(angle) * (5.5 if index % 4 == 0 else 6.5)))
        draw.line((inner, outer), fill=PALETTE["copper_light"] if index % 4 == 0 else PALETTE["tick"])
    for x, y in ((5, 10), (26, 10), (5, 23), (26, 23)):
        draw.rectangle((x, y, x + 1, y + 1), fill=PALETTE["outline"])
        draw.point((x, y), fill=PALETTE["copper_light"])
    for point in ((4, 19), (4, 20), (5, 21), (24, 26), (25, 25), (26, 24)):
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
            dy = (y - CENTER[1]) / 0.8
            along = dx * direction[0] + dy * direction[1]
            across = dx * perpendicular[0] + dy * perpendicular[1]
            if 0 <= along <= 7.2 and abs(across) <= max(0.45, 1.9 * (1 - along / 7.8)):
                front.append((x, y, across))
            elif -4.8 <= along < 0 and abs(across) <= max(0.4, 1.35 * (1 + along / 5.4)):
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
    draw.ellipse((13, 15, 18, 19), fill=PALETTE["outline"])
    draw.ellipse((14, 16, 17, 18), fill=PALETTE["copper"])
    draw.line((14, 16, 16, 16), fill=PALETTE["copper_light"])
    draw.point((16, 18), fill=PALETTE["copper_shadow"])
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
