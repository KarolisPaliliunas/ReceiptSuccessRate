package ReceiptSuccessRate
import groovy.sql.GroovyResultSet
import java.nio.file.Files
import java.sql.ResultSet

class RateCalculator {
    void calculateRate(String path){
        DbQueries dbQueries = new DbQueries()
        deleteHtmlIfExists(path)
        def businessPartnerList = dbQueries.getAllVendors(dbQueries.currentSqlInstance)
        businessPartnerList.each{ businessPartner ->
            def successfulLines = 0
            def numberOfRecords = 0
            def purchaseOrderList = dbQueries.getPurchaseOrderByBusinessPartner(dbQueries.currentSqlInstance, businessPartner.id)
            purchaseOrderList.each {purchaseOrder ->
                def purchaseOrderLineList = dbQueries.getPurchaseOrderLinesByBusinessPartner(dbQueries.currentSqlInstance, purchaseOrder.id)
                numberOfRecords += purchaseOrderLineList.size()
                purchaseOrderLineList.each {purchaseOrderLine ->
                    if (purchaseOrderLine.promisedReceiptDate == purchaseOrderLine.actualReceiptDate){
                        successfulLines += 1
                    }
                }
            }
            if(numberOfRecords != 0){
                appendHtml(path, numberOfRecords, successfulLines, businessPartner)
            }
        }
    }

    def calculatePercentage(Integer numberOfRecords, Integer successfulRecords){
        def percentage = successfulRecords/numberOfRecords * 100
        return percentage.round()
    }

    void deleteHtmlIfExists(String path){
        File file = new File("${path}/outputFile.html")
        try{
            Files.deleteIfExists(file.toPath())
        }
        catch(Exception e){
            println("exception: " + e)
        }

    }

    void appendHtml(String path, Integer numberOfRecords, Integer successfulLines, Map businessPartner){
        def successRate = calculatePercentage(numberOfRecords, successfulLines)
        File file = new File("${path}/outputFile.html")
        file.append("""
                <b>Vendor: </b> ${businessPartner.name} <br>
                <a style="margin-left:20">NumberOfPurchaseOrderLines: ${numberOfRecords}</a> <br>
                <a style="margin-left:20">NumberOfOnTimeReceivedLines: ${successfulLines}</a> <br>
                <a style="margin-left:20">Rating: ${successRate}%</a> <br>
                >---------------------------------------------------------------------< <br><br>
                """)
    }
}
