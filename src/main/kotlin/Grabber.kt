import java.io.File
import kotlin.io.path.Path
import kotlin.io.path.copyTo

const val motherEnd = "C:\\Users\\perpe\\OneDrive\\Desktop\\finals PPs"
const val motherStart = "C:\\Users\\perpe\\Arjun\\Codey\\Python\\practice_p_maker\\saves"

val addMath = Subject(19..19, listOf('m','s','w'), listOf(1,2), 2, "0606")

fun go(subj: Subject) {
    // this code looks trash but bear with me
    for (year in subj.years) {
        for (series in subj.series) {
            for (paper in subj.papers) {
                if (subj.variants == 0) {
                    println("Doing $series -  $year Paper: $paper")
                    finished = "$motherEnd\\${subj.code}\\${prettySeriesName(series)} 20${year}\\Paper ${paper}\\questions"
                    questions = "$motherStart\\${subj.code}\\${prettySeriesName(series)} 20${year}\\Paper ${paper}\\questions"
                    prime()
                    copyAnswerDir(subj.code,year,series,paper)
                } else if (series == 'm') {
                    println("Doing $series -  $year Paper: $paper")
                    finished = "$motherEnd\\${subj.code}\\${prettySeriesName(series)} 20${year}\\Paper ${paper}\\Variant 2\\questions"
                    questions = "$motherStart\\${subj.code}\\${prettySeriesName(series)} 20${year}\\Paper ${paper}\\Variant 2\\questions"
                    prime()
                    copyAnswerDir(subj.code,year,series,paper,2)
                } else {
                    for (variant in 1..subj.variants) {
                        println("Doing $series -  $year Paper: $paper/$variant")
                        finished = "$motherEnd\\${subj.code}\\${prettySeriesName(series)} 20${year}\\Paper ${paper}\\Variant ${variant}\\questions"
                        questions = "$motherStart\\${subj.code}\\${prettySeriesName(series)} 20${year}\\Paper ${paper}\\Variant ${variant}\\questions"
                        prime()
                        copyAnswerDir(subj.code,year,series,paper,variant)
                    }
                }
            }
        }
    }

    answerQuestionParity(subj.variants,subj.code)
}

fun prettySeriesName(s: Char): String {
    return when(s) {
        'm' -> "March"
        's' -> "June"
        'w' -> "November"
        else -> ""
    }
}

fun copyAnswerDir(code: String,yr: Int, series: Char, paper:Int, variant: Int = 0) {
    val source = when (variant) {
        0 -> "$motherStart\\${code}\\${prettySeriesName(series)} 20${yr}\\Paper ${paper}\\answers"
        else -> "$motherStart\\${code}\\${prettySeriesName(series)} 20${yr}\\Paper ${paper}\\Variant ${variant}\\answers"
    }
    val destination = when (variant) {
        0 -> "$motherEnd\\${code}\\${prettySeriesName(series)} 20${yr}\\Paper ${paper}\\answers"
        else -> "$motherEnd\\${code}\\${prettySeriesName(series)} 20${yr}\\Paper ${paper}\\Variant ${variant}\\answers"
    }

    val answerIn = File(source)
    try {
        answerIn.listFiles().forEach {
            Path(source + "\\${it.name}").copyTo(Path(destination+"\\${it.name}"), false)
        }
    } catch (e: Exception) {
        when (e) {
            is java.nio.file.NoSuchFileException -> println("Failed to copy answers for ${prettySeriesName(series)} - ${yr}, Paper $paper, $variant")
            is java.nio.file.FileAlreadyExistsException -> {}
            else -> throw e
        }
    }


}

fun answerQuestionParity(variants: Int, code: String) {
    val toDelete = mutableListOf<File>()

    File("$motherEnd\\$code").listFiles().forEach { seriesyr ->
        seriesyr.listFiles().forEach { paper ->
            if (variants > 0) {
                for (variant in paper.listFiles()) {
                    val q: File
                    val a: File
                    try {
                        q = variant.listFiles().first { it.name == "questions" }
                        a = variant.listFiles().first {it.name == "answers"}
                    } catch (e: NoSuchElementException) {
                        toDelete.add(variant.absoluteFile)
                        continue
                    }
                    if (q.listFiles().size != a.listFiles().size) {
                        toDelete.add(variant.absoluteFile)
                        continue
                    }
                }
            } else {
                val q: File
                val a: File
                try {
                    q = paper.listFiles().first { it.name == "questions" }
                    a = paper.listFiles().first {it.name == "answers"}
                } catch (e: NoSuchElementException) {
                    toDelete.add(paper.absoluteFile)
                    return@forEach
                }
                if (q.listFiles().size != a.listFiles().size) {
                    toDelete.add(paper.absoluteFile)
                    return@forEach
                }
            }
        }
    }

    toDelete.forEach{it.deleteRecursively()}
}

data class Subject(val years: IntRange, val series: List<Char>, val papers: List<Int>, val variants: Int, val code: String)
