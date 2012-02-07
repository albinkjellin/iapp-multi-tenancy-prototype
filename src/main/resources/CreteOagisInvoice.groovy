import groovy.xml.MarkupBuilder
/**
 * Created by IntelliJ IDEA.
 * User: albinkjellin
 * Date: 2/5/12
 * Time: 11:16 AM
 * To change this template use File | Settings | File Templates.
 */

def writer = new StringWriter()
def oagisSyncInvoice = new MarkupBuilder(writer)

oagisSyncInvoice.SyncInvoice() {
    ApplicationArea {
        Sender {
            LogicalID('Zuora')
            ComponentID('TestComponent')
        }
        CreationDateTime(new Date().format('yyyy-MM-dd\'T\'HH:mm:sssZ'))

    }
    DataArea {
        Sync {
            ActionCriteria {
                //ActionExpression('')
                ChangeStatus {
                    Code('Sync')
                    //Description('')
                    //EffectiveDateTime
                    ReasonCode('New')
                    Reason('New Invoice')
                    StateChange {
                        //FromStateCode('')
                        ToStateCode('New')
                    }
                }
            }
        }
        Invoice {
            InvoiceHeader {
                DocumentID(payload.invoiceNumber)
                AlternateDocumentID('agencyRole': 'zuora-id', payload.id)
                DocumentDateTime(payload.createdDate)
                //Description()
                //Note()
                TotalAmount('currencyID': payload.account.currency, payload.amount)
                CustomerParty {
                    PartyIDs {
                        ID(payload.account.accountNumber)
                        Name(payload.account.name)
                        Address {
                            AddressLine(payload.billTo.address1)
                            CitySubDivisionName(payload.billTo.state)
                            CityName(payload.billTo.city)
                            CountryCode(payload.billTo.country)
                            PostalCode(payload.billTo.postalCode)
                        }
                    }
                }
                PaymentTerm('type': payload.account.paymentTerm) {
                    Term {
                        DueDateTime(payload.dueDate)
                    }
                }
                if (payload.taxExemptAmount) {
                    Tax {
                        Exemption {
                            Amount(payload.taxExemptAmount)
                        }
                        Amount(payload.taxAmount)
                    }
                }
            }
            payload.invoiceitems.each {
                def line = it
                Item {
                    ItemID(line.SKU)
                }
                Description(line.productDescription)
                Quantity('unitCode': line.UOM, line.quantity)
                UnitPrice {
                    Amount('currencyID': payload.account.currency, line.unitPrice)
                    PerQuantity('unitCode': 'Each', 1)
                    TimePeriod {
                        StartDateTime(line.serviceStartDate)
                        EndDateTime(line.serviceEndDate)
                    }
                }
                TotalAmount('currencyID': payload.account.currency, line.chargeAmount)
                def seq = 1
                line.taxationitems.each {
                    def tax = it
                    Tax('sequenceNumber': seq) {
                        ID(tax.name)
                        TaxJurisdicationCodes {
                            Code('name': 'Jurisdiction', tax.jurisdiction)
                            Code('name': 'Location', tax.locationCode)
                            Code('name': 'TaxCode', tax.taxCode)
                        }
                    }
                    Calculation {
                        RateNumeric(tax.taxRate)
                    }
                    Exemption {
                        Amount('currencyID': payload.account.currency, tax.exemptAmount)

                    }
                    Amount('currencyID': payload.account.currency, tax.taxAmount)
                }
            }
        }
    }
}

return writer.toString()