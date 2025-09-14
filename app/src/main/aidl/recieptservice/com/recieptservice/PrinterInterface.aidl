package recieptservice.com.recieptservice;

interface PrinterInterface {
    void beginWork();
    void endWork();
    void printText(String text);
    void nextLine(int lines);
    void setAlignment(int align);
    void printBarCode(String data, int symbology, int height, int width);
    void printQRCode(String data, int moduleSize, int ecLevel);
}