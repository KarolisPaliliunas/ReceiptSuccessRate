package ReceiptSuccessRate

import groovy.sql.GroovyResultSet
import groovy.sql.Sql
import javax.sql.rowset.CachedRowSet
import java.sql.ResultSet

class DbQueries {
    Sql currentSqlInstance

    DbQueries(){
        def dbName = "Database_Test"

        def url = "jdbc:postgresql://localhost:5432/$dbName"
        def user = 'postgres'
        def password = 'postgres'
        def driver = 'org.postgresql.Driver'
        currentSqlInstance = Sql.newInstance(url, user, password, driver)
    }

    List<Map> getAllVendors(Sql receivedSqlInstance){
        def businessPartnerList = []

        receivedSqlInstance.eachRow("""
                SELECT id, name FROM 
                business_partner 
                WHERE is_vendor = true AND  active = true
            """){ businessPartner -> businessPartnerList.add([id:businessPartner.id, name:businessPartner.name])}
        return businessPartnerList
   }

    List<Map> getPurchaseOrderByBusinessPartner(Sql receivedSqlInstance, Long businessPartnerId){
        List<Map> purchaseOrderList = []
        receivedSqlInstance.eachRow("""
                    SELECT id, order_no FROM purchase_order
                    WHERE business_partner_id = $businessPartnerId
                    AND status = 30
                """) {purchaseOrder -> purchaseOrderList.add(['id':purchaseOrder.id, 'orderNo':purchaseOrder.order_no])}
        return purchaseOrderList
    }

    List<Map> getPurchaseOrderLinesByBusinessPartner(Sql receivedSqlInstance, Long purchaseOrderId){
        List<Map> purchaseOrderLineList = []
        receivedSqlInstance.eachRow("""
                    SELECT id, promised_receipt_date, receipt_date FROM purchase_order_line
                    WHERE purchase_order_id = $purchaseOrderId
                """) {purchaseOrderLine -> purchaseOrderLineList.add(['id':purchaseOrderLine.id, 'promisedReceiptDate':purchaseOrderLine.promised_receipt_date, 'actualReceiptDate':purchaseOrderLine.receipt_date])}
        return purchaseOrderLineList
    }
}
