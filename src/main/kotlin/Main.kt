import java.awt.image.BufferedImage
import java.awt.image.RasterFormatException
import java.io.File
import javax.imageio.ImageIO


const val finished = "C:\\Users\\perpe\\IdeaProjects\\WhiteSpacer\\out"
const val questions = "questions/"

const val topThreshold: Int = 20
const val bottomThreshold: Int = 20

fun main() {
    prime()
}

fun prime() {
    val qDir = Thread.currentThread().contextClassLoader.getResource(questions)
    File(qDir?.toURI()).listFiles()?.forEach { file ->
        val imgs: List<BufferedImage>?

        if (file.extension == "png") {
            imgs = fixed(ImageIO.read(file))
        } else {
            return@forEach
        }

        if (imgs.isEmpty()) return@forEach

        val joinedImg = BufferedImage(
            imgs[0].width,
            imgs.sumOf { return@sumOf it.height },
            BufferedImage.TYPE_INT_ARGB
        )
        val g2 = joinedImg.createGraphics()

        var currentHeight = 0

        imgs.forEach{img ->
            g2.drawImage(img, null, 0, currentHeight)
            currentHeight += img.height
        }

        g2.dispose()
        ImageIO.write(joinedImg,"png",File("$finished\\${file.name}")) //todo merging array of screenshots
    }



}

fun fixed(brokenPg: BufferedImage): MutableList<BufferedImage> {
    val coloredRegions = mutableListOf<BufferedImage>()
    var isWhite = true
    var lastColouredRow = -1
    var colouredTop = -1

    val pg = fiximg(brokenPg)

    for (y in 0..<pg.height) {
        val isColouredRow = rowHasColour(pg, y) // checks if the row has any coloured pixels (a way to look for diagrams or text)
        if (isColouredRow) {
            lastColouredRow = y
            if (isWhite) { // this checks if the last few lines have been devoid of any content
                colouredTop = y
                isWhite = false
            }

            continue
        }

        if (!isWhite) { // only triggers when it thinks its going thru a region w/content
            if (y - lastColouredRow > bottomThreshold) { //todo handle 1st colour case
                //^it checks the distance between current row and last row with colour and checks if its there hasnt been content for long
                try {
                coloredRegions.add(pg.getSubimage(0,
                    (colouredTop - topThreshold).coerceAtLeast(0)
                    , pg.width, y - colouredTop))
                } catch (e: RasterFormatException) {
                    // so i add some neighbouring white pixels for some spacing and sometimes that goes out of bounds
                    // typically only happens with the final subject code and turn over n stuff (useless) so this is just there
                    // so the program doesnt go "HEYHEY YOURE TRYING TO ACCESS AND IMG THAT ISNT THERE"
                    continue
                } finally {
                    isWhite = true
                }
            }
        }

    }

    return coloredRegions
}

fun fiximg(img: BufferedImage): BufferedImage {
    val rgbImage = BufferedImage(img.width, img.height, BufferedImage.TYPE_INT_RGB)
    val g = rgbImage.createGraphics()
    g.drawImage(img, 0, 0, null)
    g.dispose()

    return rgbImage
}

fun rowHasColour(img: BufferedImage, row: Int): Boolean {
    return img.getRGB(0, row, img.width, 1, null, 0, img.width).distinct().size >= 2
}


