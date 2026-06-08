package com.codewithmd.blogger.bloggerappsapis.config;

@Service
@RequiredArgsConstructor
public class SesEmailService {

    private final SesClient sesClient;
    private final Logger logger = LoggerFactory.getLogger(SesEmailService.class);

    @Value("${aws.ses.sender-email}")
    private String senderEmail;

    public void sendBulkUploadSummary(String toEmail, BulkStatus bulkStatus, Integer jobId) {
        try {
            String subject = buildSubject(bulkStatus);
            String htmlBody = buildHtmlBody(bulkStatus, jobId);

            SendEmailRequest request = SendEmailRequest.builder()
                    .destination(Destination.builder()
                            .toAddresses(toEmail)
                            .build())
                    .message(Message.builder()
                            .subject(Content.builder()
                                    .data(subject)
                                    .charset("UTF-8")
                                    .build())
                            .body(Body.builder()
                                    .html(Content.builder()
                                            .data(htmlBody)
                                            .charset("UTF-8")
                                            .build())
                                    .build())
                            .build())
                    .source(senderEmail)
                    .build();

            sesClient.sendEmail(request);
            logger.info("✅ Email sent to {} for job {}", toEmail, jobId);

        } catch (Exception e) {
            logger.error("❌ Failed to send email for job {}: {}", jobId, e.getMessage());
        }
    }

    private String buildSubject(BulkStatus status) {
        if ("COMPLETED".equals(status.getStatus())) {
            return "✅ Bulk Upload Completed — All " + status.getTotalFiles() + " files processed!";
        } else {
            return "⚠️ Bulk Upload Partially Failed — "
                    + status.getFailedFiles() + " of "
                    + status.getTotalFiles() + " files failed";
        }
    }

    private String buildHtmlBody(BulkStatus bulkStatus, Integer jobId) {
        StringBuilder sb = new StringBuilder();
        sb.append("""
            <div style="font-family: Arial, sans-serif; max-width: 600px; margin: auto;">
                <div style="background: #f0a500; padding: 20px; border-radius: 8px 8px 0 0;">
                    <h2 style="color: white; margin: 0;">📊 Bulk Upload Summary</h2>
                    <p style="color: white; margin: 5px 0;">Job ID: %s</p>
                </div>
                <div style="background: #f9f9f9; padding: 20px;">
        """.formatted(jobId));

        // Stats
        sb.append("""
            <div style="display: flex; gap: 16px; margin-bottom: 20px;">
                <div style="flex:1; background: white; padding: 16px; border-radius: 8px; text-align: center; border: 1px solid #eee;">
                    <div style="font-size: 28px; font-weight: bold;">%d</div>
                    <div style="color: #666;">Total</div>
                </div>
                <div style="flex:1; background: white; padding: 16px; border-radius: 8px; text-align: center; border: 1px solid #eee;">
                    <div style="font-size: 28px; font-weight: bold; color: #28a745;">%d</div>
                    <div style="color: #666;">Processed</div>
                </div>
                <div style="flex:1; background: white; padding: 16px; border-radius: 8px; text-align: center; border: 1px solid #eee;">
                    <div style="font-size: 28px; font-weight: bold; color: #dc3545;">%d</div>
                    <div style="color: #666;">Failed</div>
                </div>
            </div>
        """.formatted(
                bulkStatus.getTotalFiles(),
                bulkStatus.getProcessedFiles(),
                bulkStatus.getFailedFiles()
        ));

        // File details table
        sb.append("""
            <h3 style="color: #333;">File Details</h3>
            <table style="width:100%; border-collapse: collapse;">
                <thead>
                    <tr style="background: #f0a500; color: white;">
                        <th style="padding: 10px; text-align: left;">File Name</th>
                        <th style="padding: 10px; text-align: center;">Status</th>
                        <th style="padding: 10px; text-align: left;">Message</th>
                    </tr>
                </thead>
                <tbody>
        """);

        for (FileUploadStatus file : bulkStatus.getFileUploadStatus()) {
            String color = "SUCCESS".equals(file.getStatus()) ? "#28a745" :
                    "FAILED".equals(file.getStatus())  ? "#dc3545" : "#666";
            String icon  = "SUCCESS".equals(file.getStatus()) ? "✅" :
                    "FAILED".equals(file.getStatus())  ? "❌" : "📄";

            sb.append("""
                <tr style="border-bottom: 1px solid #eee;">
                    <td style="padding: 10px;">%s %s</td>
                    <td style="padding: 10px; text-align: center;">
                        <span style="color: %s; font-weight: bold;">%s</span>
                    </td>
                    <td style="padding: 10px; color: #dc3545; font-size: 12px;">%s</td>
                </tr>
            """.formatted(
                    icon,
                    file.getFileName(),
                    color,
                    file.getStatus(),
                    file.getError_message() != null ? file.getError_message() : ""
            ));
        }

        sb.append("""
                </tbody>
            </table>
        </div>
        <div style="background: #333; padding: 16px; border-radius: 0 0 8px 8px; text-align: center;">
            <p style="color: #aaa; margin: 0; font-size: 13px;">
                BloggerHub — Automated Notification
            </p>
        </div>
    </div>
        """);

        return sb.toString();
    }
}