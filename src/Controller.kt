import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.FileReader
import java.io.FileWriter
import java.util.Locale.getDefault
import kotlin.math.log


fun main() {
    val controller = Controller()
    controller.mainLoop()
}

private const val INPUT_FILE_PATH = "newAsTSV.tsv"
private const val OUTPUT_FILE_PATH = "newOutAsTSV.tsv"
private val HEADERS_INDEXES = mapOf(
    "Leverantör" to 0,
    "Datum levererad" to 1,
    "Datum inlagt i dokumentet" to 2,
    "Order/faktura nummer" to 3,
    "Bokförd (mån)" to 4,
    "Ordertotal (totalvärde)" to 5,
    "Modellnamn" to 6,
    "Förväntat antal" to 7,
    "Pris" to 8,
    "Hyllplatser" to 9,
    "Kommentar" to 10,
    "Installation" to 11,
    "Specifikationer" to 12,
    "Klassificering" to 13,
    "Aktiverat antal" to 14,
    "Återstår oaktiverat" to 15,
    "Aktiveringsdatum" to 16,
    "Namn i PS" to 17,
    "Allt hanterat (datum)" to 18,
    "Lagervärde" to 19
)



class Controller {
    var unsortedTable: ArrayList<ArrayList<String>> = ArrayList()
    var sortedTable: ArrayList<ArrayList<String>> = ArrayList()

    fun mainLoop() {
       this.makeTablesFromFile()
        while (true) {
            val selection: ArrayList<ArrayList<String>> =
                this.makeSelection(ArrayList(this.unsortedTable), null)
            val specsToAppend = this.categorizeSelection()
            this.commitChanges(selection, specsToAppend)
            this.printToFile()

        }
    }


    private fun commitChanges(selection: ArrayList<ArrayList<String>>, specs: ArrayList<String>) {
        selection.forEach { row ->
            row.addAll(specs)
            val cloneRow = ArrayList<String>()
            row.forEach { cell -> cloneRow.add(cell) }
            this.sortedTable.add(cloneRow)
            row.clear()
        }
        this.unsortedTable = this.unsortedTable.filter { row -> row.isNotEmpty() } as ArrayList<ArrayList<String>>
    }

    private fun printToFile() {
        val bw = BufferedWriter(FileWriter(OUTPUT_FILE_PATH))
        bw.write(HEADERS_INDEXES.keys.joinToString("\t"))
        bw.append("\t")
        bw.append("Brand")
        bw.append("\t")
        bw.append("Model")
        bw.append("\t")
        bw.append("CPU")
        bw.append("\t")
        bw.append("GPU")
        bw.append("\t")
        bw.append("Type")


        bw.newLine()

        this.sortedTable.forEach { row ->
            bw.write(row.joinToString("\t"))
            bw.newLine()
        }
        this.unsortedTable.forEach { row ->
            bw.write(row.joinToString("\t"))
            bw.newLine()
        }
        bw.flush()
        bw.close()
    }





    private fun makeTablesFromFile() {
        val bufferedReader = BufferedReader(FileReader(INPUT_FILE_PATH))
        var line: String? = bufferedReader.readLine()
        while (line != null ) {
            val lineParts = ArrayList(line.split("\t"))
            while (lineParts.size < HEADERS_INDEXES.size) {
                lineParts.add("")
            }
            if (lineParts.size <= 17) {
                this.unsortedTable.add(ArrayList(lineParts))
            } else {
                this.sortedTable.add(ArrayList(lineParts))
            }
            line = bufferedReader.readLine()
        }
    }


    private fun makeSelection(selectionInput: ArrayList<ArrayList<String>>, userInput: String?): ArrayList<ArrayList<String>> {
        val descriptionIndex: Int = HEADERS_INDEXES["Modellnamn"] as Int
        var criteria :String? = userInput

        if(criteria == null) {
            selectionInput.forEach { println(it) }
            println("Make a filter criteria")
            criteria = readln()
        }

        val selection = selectionInput.filter { row ->
            row[descriptionIndex].lowercase(getDefault()).contains(Regex(criteria, RegexOption.IGNORE_CASE))
        } as ArrayList<ArrayList<String>>
        println("Rows Selected: " + selection.size)
        selection.forEach { row -> println(row) }

        println("")
        println("type d if you are done with your selection")
        println("type r if you want to restart from the base table")
        val userInput = readln()

        return when (userInput.lowercase()) {
            "d" -> selection
            "r" -> makeSelection( ArrayList(this.unsortedTable.filter{ row->row.size<=HEADERS_INDEXES.size}),null)
            else -> makeSelection(selection,userInput)
        }

    }

    private fun categorizeSelection(): ArrayList<String> {
        val specs = ArrayList<String>()

        println("Brand?")
        specs.add(readln().lowercase().trim())
        println("Model?")
        specs.add(readln().lowercase().trim())
        println("CPU?")
        specs.add(readln().lowercase().trim())
        println("GPU?")
        specs.add(readln().lowercase().trim())
        specs.add(getUserInputRam())
        specs.add(getUserInputStorage())
        specs.add(readln().lowercase().trim())

        specs.add(getUserInputProductType())

        return specs
    }

    private fun getUserInputStorage(): String {
        println("Storage Size? (GB)")

        try {
            val storageSize = Integer.parseInt(readln())
            val log = log(storageSize.toDouble(), 2.0)
            if (log % 1 != 0.0) {
                println("Storage-size expected to be power of two (128, 256,512, 1024 etc).")
                println("Type f to force entry, Type any other key to re-enter input")
                val force = readln().lowercase().trim()
                return if (force == "f") {
                    storageSize.toString()
                } else {
                    getUserInputRam()
                }
            }
            return storageSize.toString()
        } catch (e: NumberFormatException) {
            println("Please enter a valid integer")
            e.printStackTrace()
            return getUserInputRam()
        }
    }




    private fun getUserInputRam () : String {
        println("RAM Size? (GB)")
        try {
            val ramSize = Integer.parseInt( readln())
            val log = log(ramSize.toDouble(),2.0)
            if (log % 1 != 0.0) {
                println("Ram-size expected to be power of two (2, 4,8,16,32 etc).")
                println("Type f to force entry, Type any other key to re-enter input")
                val force = readln().lowercase().trim()
                return if (force == "f") {
                    ramSize.toString()
                } else {
                    getUserInputRam()
                }
            }
            return ramSize.toString()
        } catch (e: NumberFormatException) {
            println("Please enter a valid integer")
            e.printStackTrace()
            return getUserInputRam()
        }
    }

    private fun getUserInputProductType() : String {
        println("")
        println("Type? desktop, laptop, aio, phone, or other")
        when(readln().lowercase()) {
            "desktop" -> return "Desktop"
            "laptop" -> return "Laptop"
            "aio" -> return "Aio"
            "phone" -> return "Phone"
            "other" -> return "Other"
            else -> {
                print("invalid input")
                return getUserInputProductType()
            }
        }
    }
}

