package com.hotel.gestion;

import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.hotel.gestion.db.DatabaseHelper;
import com.hotel.gestion.models.ConsommationService;
import com.hotel.gestion.utils.MoneyUtils;

import java.io.File;
import java.io.FileOutputStream;

public class FactureActivity extends AppCompatActivity {
    public static final String EXTRA_RESERVATION_ID = "reservation_id";

    private DatabaseHelper dbHelper;
    private DatabaseHelper.ReservationInvoice currentInvoice;
    private long reservationId;
    private TextView textClientName;
    private TextView textClientContact;
    private TextView textClientPassport;
    private TextView textClientAddress;
    private TextView textRoomInfo;
    private TextView textRoomDates;
    private TextView textRoomDuration;
    private TextView textRoomSubtotal;
    private TextView textServiceSubtotal;
    private TextView textInvoiceTotal;
    private LinearLayout servicesContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_facture);

        reservationId = getIntent().getLongExtra(EXTRA_RESERVATION_ID, -1L);
        if (reservationId <= 0) {
            finish();
            return;
        }

        dbHelper = new DatabaseHelper(this);
        textClientName = findViewById(R.id.textInvoiceClientName);
        textClientContact = findViewById(R.id.textInvoiceClientContact);
        textClientPassport = findViewById(R.id.textInvoiceClientPassport);
        textClientAddress = findViewById(R.id.textInvoiceClientAddress);
        textRoomInfo = findViewById(R.id.textInvoiceRoomInfo);
        textRoomDates = findViewById(R.id.textInvoiceDates);
        textRoomDuration = findViewById(R.id.textInvoiceDuration);
        textRoomSubtotal = findViewById(R.id.textInvoiceRoomSubtotal);
        textServiceSubtotal = findViewById(R.id.textInvoiceServiceSubtotal);
        textInvoiceTotal = findViewById(R.id.textInvoiceGrandTotal);
        servicesContainer = findViewById(R.id.layoutInvoiceServices);

        Button buttonCloseStay = findViewById(R.id.buttonCloseStay);
        Button buttonShareInvoice = findViewById(R.id.buttonShareInvoice);
        buttonShareInvoice.setOnClickListener(v -> exportInvoicePdf());
        buttonCloseStay.setOnClickListener(v -> closeStay());
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadInvoice();
    }

    private void loadInvoice() {
        DatabaseHelper.ReservationInvoice invoice = dbHelper.getReservationInvoice(reservationId);
        if (invoice == null) {
            Toast.makeText(this, getString(R.string.message_invoice_missing), Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        currentInvoice = invoice;

        textClientName.setText(invoice.client.getNomComplet().trim());
        textClientContact.setText(getString(
                R.string.invoice_client_contact_format,
                emptyIfNull(invoice.client.getTelephone()),
                emptyIfNull(invoice.client.getEmail())
        ));
        textClientPassport.setText(getString(
                R.string.invoice_passport_format,
                emptyIfNull(invoice.client.getNumeroPasseport())
        ));
        textClientAddress.setText(getString(
                R.string.invoice_address_format,
                emptyIfNull(invoice.client.getAdresse())
        ));
        textRoomInfo.setText(getString(
                R.string.invoice_room_format,
                invoice.chambre.getNumero(),
                invoice.chambre.getType()
        ));
        textRoomDates.setText(getString(
                R.string.invoice_dates_format,
                invoice.reservation.getDateDebut(),
                invoice.reservation.getDateFin()
        ));
        textRoomDuration.setText(getString(R.string.nights_format, invoice.nights));
        textRoomSubtotal.setText(MoneyUtils.formatMoney(invoice.roomTotal));
        textServiceSubtotal.setText(MoneyUtils.formatMoney(invoice.serviceTotal));
        textInvoiceTotal.setText(MoneyUtils.formatMoney(invoice.grandTotal));

        servicesContainer.removeAllViews();
        if (invoice.consommations.isEmpty()) {
            TextView emptyView = new TextView(this);
            emptyView.setText(R.string.empty_services);
            emptyView.setTextSize(11);
            emptyView.setTextColor(getColor(R.color.text_secondary));
            servicesContainer.addView(emptyView);
            return;
        }

        for (ConsommationService item : invoice.consommations) {
            LinearLayout row = (LinearLayout) LayoutInflater.from(this)
                    .inflate(R.layout.item_facture_service, servicesContainer, false);
            TextView textName = row.findViewById(R.id.textInvoiceServiceName);
            TextView textQty = row.findViewById(R.id.textInvoiceServiceQty);
            TextView textTotal = row.findViewById(R.id.textInvoiceServiceTotal);
            textName.setText(item.getNomService());
            textQty.setText("x" + item.getQuantite());
            textTotal.setText(MoneyUtils.formatMoney(item.getSousTotal()));
            servicesContainer.addView(row);
        }
    }

    private void closeStay() {
        boolean success = dbHelper.closeStayTransaction(reservationId);
        if (!success) {
            Toast.makeText(this, getString(R.string.message_close_stay_failed), Toast.LENGTH_SHORT).show();
            return;
        }

        Toast.makeText(this, getString(R.string.message_close_stay_success), Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    private void exportInvoicePdf() {
        if (currentInvoice == null) {
            Toast.makeText(this, getString(R.string.message_invoice_missing), Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            File pdfFile = generateInvoicePdf(currentInvoice);
            Toast.makeText(this, getString(R.string.message_pdf_exported), Toast.LENGTH_SHORT).show();

            Uri uri = FileProvider.getUriForFile(
                    this,
                    getPackageName() + ".fileprovider",
                    pdfFile
            );

            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("application/pdf");
            shareIntent.putExtra(
                    Intent.EXTRA_SUBJECT,
                    getString(R.string.invoice_share_subject, currentInvoice.client.getNomComplet().trim())
            );
            shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            startActivity(Intent.createChooser(shareIntent, getString(R.string.invoice_share_chooser)));
        } catch (Exception exception) {
            Toast.makeText(this, getString(R.string.message_pdf_failed), Toast.LENGTH_SHORT).show();
        }
    }

    private File generateInvoicePdf(DatabaseHelper.ReservationInvoice invoice) throws Exception {
        File baseDir = getExternalFilesDir("documents");
        if (baseDir == null) {
            baseDir = getFilesDir();
        }
        File pdfDir = new File(baseDir, "factures");
        if (!pdfDir.exists() && !pdfDir.mkdirs()) {
            throw new IllegalStateException("Impossible de creer le dossier PDF.");
        }

        File pdfFile = new File(pdfDir, "facture_" + reservationId + ".pdf");
        PdfDocument document = new PdfDocument();

        int pageWidth = 595;
        int pageHeight = 842;
        int margin = 36;
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create();
        PdfDocument.Page page = document.startPage(pageInfo);
        Canvas canvas = page.getCanvas();

        Paint backgroundPaint = new Paint();
        backgroundPaint.setColor(Color.WHITE);
        canvas.drawRect(0, 0, pageWidth, pageHeight, backgroundPaint);

        Paint headerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        headerPaint.setColor(Color.parseColor("#1A1A2E"));
        canvas.drawRect(0, 0, pageWidth, 120, headerPaint);

        Paint accentPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        accentPaint.setColor(Color.parseColor("#185FA5"));

        Paint whiteTitlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        whiteTitlePaint.setColor(Color.WHITE);
        whiteTitlePaint.setTextSize(24f);
        whiteTitlePaint.setFakeBoldText(true);

        Paint whiteBodyPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        whiteBodyPaint.setColor(Color.parseColor("#D7DFF0"));
        whiteBodyPaint.setTextSize(11f);

        Paint sectionTitlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        sectionTitlePaint.setColor(Color.parseColor("#1A1A2E"));
        sectionTitlePaint.setTextSize(13f);
        sectionTitlePaint.setFakeBoldText(true);

        Paint bodyPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        bodyPaint.setColor(Color.parseColor("#4F5663"));
        bodyPaint.setTextSize(11f);

        Paint amountPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        amountPaint.setColor(Color.parseColor("#185FA5"));
        amountPaint.setTextSize(18f);
        amountPaint.setFakeBoldText(true);

        Paint dividerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        dividerPaint.setColor(Color.parseColor("#E0E4EB"));
        dividerPaint.setStrokeWidth(1f);

        Paint cardPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        cardPaint.setColor(Color.parseColor("#F7F9FC"));

        canvas.drawText("HOTEL MANAGER", margin, 42, whiteTitlePaint);
        canvas.drawText("Facture de sejour", margin, 66, whiteBodyPaint);
        canvas.drawText("Generee le " + dbHelper.getCurrentDate(), margin, 86, whiteBodyPaint);
        canvas.drawText(MoneyUtils.formatMoney(invoice.grandTotal), margin, 108, amountPaint);

        float cursorY = 150f;
        cursorY = drawInfoCard(canvas, margin, cursorY, pageWidth - margin, invoice, sectionTitlePaint, bodyPaint, cardPaint, dividerPaint);
        cursorY += 16f;
        cursorY = drawServicesCard(canvas, margin, cursorY, pageWidth - margin, invoice, sectionTitlePaint, bodyPaint, cardPaint, dividerPaint);
        cursorY += 16f;
        drawTotalsCard(canvas, margin, cursorY, pageWidth - margin, invoice, sectionTitlePaint, bodyPaint, amountPaint, cardPaint);

        document.finishPage(page);

        FileOutputStream outputStream = new FileOutputStream(pdfFile);
        document.writeTo(outputStream);
        outputStream.flush();
        outputStream.close();
        document.close();
        return pdfFile;
    }

    private float drawInfoCard(Canvas canvas, float left, float top, float right,
                               DatabaseHelper.ReservationInvoice invoice, Paint titlePaint,
                               Paint bodyPaint, Paint cardPaint, Paint dividerPaint) {
        float height = 168f;
        RectF rect = new RectF(left, top, right, top + height);
        canvas.drawRoundRect(rect, 16f, 16f, cardPaint);

        float x = left + 18f;
        float y = top + 24f;
        canvas.drawText("Informations client", x, y, titlePaint);
        y += 20f;
        canvas.drawText("Nom : " + invoice.client.getNomComplet().trim(), x, y, bodyPaint);
        y += 16f;
        canvas.drawText("Telephone : " + emptyIfNull(invoice.client.getTelephone()), x, y, bodyPaint);
        y += 16f;
        canvas.drawText("Email : " + emptyIfNull(invoice.client.getEmail()), x, y, bodyPaint);
        y += 16f;
        canvas.drawText("Passeport : " + emptyIfNull(invoice.client.getNumeroPasseport()), x, y, bodyPaint);

        y += 18f;
        canvas.drawLine(x, y, right - 18f, y, dividerPaint);
        y += 20f;

        canvas.drawText("Informations chambre", x, y, titlePaint);
        y += 20f;
        canvas.drawText("Chambre : " + invoice.chambre.getNumero() + " - " + invoice.chambre.getType(), x, y, bodyPaint);
        y += 16f;
        canvas.drawText("Sejour : " + invoice.reservation.getDateDebut() + " -> " + invoice.reservation.getDateFin(), x, y, bodyPaint);
        y += 16f;
        canvas.drawText("Duree : " + invoice.nights + " nuit(s)", x, y, bodyPaint);
        return rect.bottom;
    }

    private float drawServicesCard(Canvas canvas, float left, float top, float right,
                                   DatabaseHelper.ReservationInvoice invoice, Paint titlePaint,
                                   Paint bodyPaint, Paint cardPaint, Paint dividerPaint) {
        int serviceCount = Math.max(invoice.consommations.size(), 1);
        float height = 52f + (serviceCount * 18f) + 18f;
        RectF rect = new RectF(left, top, right, top + height);
        canvas.drawRoundRect(rect, 16f, 16f, cardPaint);

        float x = left + 18f;
        float y = top + 24f;
        canvas.drawText("Services consommes", x, y, titlePaint);
        y += 20f;

        if (invoice.consommations.isEmpty()) {
            canvas.drawText("Aucun service consomme", x, y, bodyPaint);
            return rect.bottom;
        }

        for (ConsommationService item : invoice.consommations) {
            canvas.drawText(
                    item.getNomService() + " x" + item.getQuantite() + " - " + MoneyUtils.formatMoney(item.getSousTotal()),
                    x,
                    y,
                    bodyPaint
            );
            y += 18f;
        }

        canvas.drawLine(x, y, right - 18f, y, dividerPaint);
        return rect.bottom;
    }

    private void drawTotalsCard(Canvas canvas, float left, float top, float right,
                                DatabaseHelper.ReservationInvoice invoice, Paint titlePaint,
                                Paint bodyPaint, Paint amountPaint, Paint cardPaint) {
        RectF rect = new RectF(left, top, right, top + 102f);
        canvas.drawRoundRect(rect, 16f, 16f, cardPaint);

        float x = left + 18f;
        float y = top + 24f;
        canvas.drawText("Recapitulatif", x, y, titlePaint);
        y += 22f;
        canvas.drawText("Sous-total chambre : " + MoneyUtils.formatMoney(invoice.roomTotal), x, y, bodyPaint);
        y += 18f;
        canvas.drawText("Sous-total services : " + MoneyUtils.formatMoney(invoice.serviceTotal), x, y, bodyPaint);
        y += 26f;
        canvas.drawText("Total general : " + MoneyUtils.formatMoney(invoice.grandTotal), x, y, amountPaint);
    }

    private String emptyIfNull(String value) {
        return value == null || value.isBlank() ? getString(R.string.unknown_value) : value;
    }
}
