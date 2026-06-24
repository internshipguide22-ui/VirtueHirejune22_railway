package com.virtuehire.service;

import com.virtuehire.model.Hr;
import com.virtuehire.repository.HrRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class HrService {

    private static final Logger logger = LoggerFactory.getLogger(HrService.class);

    private final HrRepository repo;

    @org.springframework.beans.factory.annotation.Autowired
    private org.springframework.mail.javamail.JavaMailSender mailSender;

    @org.springframework.beans.factory.annotation.Value("${app.mail.from:}")
    private String mailFrom;

    @org.springframework.beans.factory.annotation.Value("${spring.mail.host:}")
    private String mailHost;

    @org.springframework.beans.factory.annotation.Value("${spring.mail.username:}")
    private String mailUsername;

    @org.springframework.beans.factory.annotation.Value("${spring.mail.password:}")
    private String mailPassword;

    @org.springframework.beans.factory.annotation.Value("${BREVO_API_KEY:}")
    private String brevoApiKey;

    public HrService(HrRepository repo) {
        this.repo = repo;
    }

    // ------------------ SEND VERIFICATION MAIL ------------------
    public void sendVerificationMail(Hr hr) {
        // Set registration timestamp for 3-month free trial tracking
        if (hr.getRegisteredAt() == null) {
            hr.setRegisteredAt(LocalDateTime.now());
        }

        // Generate 6-digit OTP
        String code = String.valueOf(new java.util.Random().nextInt(900000) + 100000);
        hr.setVerificationCode(code);
        repo.save(hr);

        sendEmail(hr.getEmail(), "VirtueHire HR Verification",
                "Hello " + hr.getFullName() + ",\n\n"
                + "Please use the following code to verify your HR account:\n\n"
                + "Verification Code: " + code + "\n\n"
                + "Once verified, you'll have full access to the VirtueHire HR portal for 3 months, free of charge!\n\n"
                + "Thank you,\nVirtueHire Team");
    }

    // ------------------ VERIFY EMAIL ------------------
    public boolean verifyEmail(String email, String code) {
        Hr hr = repo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("HR not found"));

        if (hr.getVerificationCode() != null && hr.getVerificationCode().equals(code)) {
            hr.setVerificationCode(null);
            hr.setEmailVerified(true);
            repo.save(hr);
            return true;
        }
        return false;
    }

    public void resendVerificationMail(String email) {
        Hr hr = repo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("HR not found"));

        if (Boolean.TRUE.equals(hr.getEmailVerified())) {
            throw new RuntimeException("Email is already verified");
        }

        sendVerificationMail(hr);
    }

    // ------------------ SEND APPROVAL MAIL ------------------
    public void sendApprovalMail(Hr hr) {
        sendEmail(hr.getEmail(), "VirtueHire HR Account Approved",
                "Hello " + hr.getFullName() + ",\n\n"
                        + "Your HR account has been approved by the VirtueHire admin team.\n"
                        + "You can now log in and access the HR portal.\n\n"
                        + "Thank you,\nVirtueHire Team");
    }

    private void sendEmail(String to, String subject, String body) {
        if (brevoApiKey != null && !brevoApiKey.isBlank()) {
            logger.info("Sending HR email to {} using Brevo REST API", to);
            sendEmailWithBrevoApi(to, subject, body);
            return;
        }

        if (mailUsername == null || mailUsername.isBlank() || mailPassword == null || mailPassword.isBlank()) {
            throw new IllegalStateException(
                    "Mail is not configured. Set MAIL_USERNAME and MAIL_PASSWORD, or set BREVO_API_KEY.");
        }

        logger.info("Sending HR email to {} using SMTP host {}", to, mailHost);
        org.springframework.mail.SimpleMailMessage message = new org.springframework.mail.SimpleMailMessage();
        if (mailFrom != null && !mailFrom.isBlank()) {
            message.setFrom(mailFrom);
        }
        message.setTo(to);
        message.setSubject(subject);
        message.setText(body);
        mailSender.send(message);
    }

    private void sendEmailWithBrevoApi(String to, String subject, String body) {
        String from = mailFrom != null && !mailFrom.isBlank() ? mailFrom : "no-reply@virtuehire.in";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        headers.set("api-key", brevoApiKey);
        headers.set("X-Mailin-api-key", brevoApiKey);

        Map<String, Object> payload = Map.of(
                "sender", Map.of(
                        "name", "VirtueHire",
                        "email", from),
                "to", List.of(Map.of("email", to)),
                "subject", subject,
                "textContent", body);

        new RestTemplate().postForEntity(
                "https://api.brevo.com/v3/smtp/email",
                new HttpEntity<>(payload, headers),
                String.class);
    }

    // ------------------ ACCESS CONTROL ------------------
    /**
     * Free trial: full access for 3 months from registration date.
     * After that, must have an active paid plan.
     */
    public boolean isAccessAllowed(Hr hr) {
        // Active paid plan → always allowed
        if (canViewCandidateDetails(hr)) return true;

        // Free trial → within 3 months of registration
     if (hr.getRegisteredAt() != null &&
        hr.getRegisteredAt().plusMonths(3).isAfter(LocalDateTime.now())) {
    return true;
}

        return false;
    }

    // ------------------ CRUD ------------------
    public Hr save(Hr hr) {
        return repo.save(hr);
    }

    public List<Hr> findAll() {
        return repo.findAll();
    }

    public Optional<Hr> findById(Long id) {
        return repo.findById(id);
    }

    public Optional<Hr> findByEmail(String email) {
        return repo.findByEmail(email);
    }

    public Hr login(String email, String password) {
        return repo.findByEmailAndPassword(email, password);
    }

    public void deleteHrById(Long id) {
        if (!repo.existsById(id)) {
            throw new RuntimeException("HR account not found");
        }
        repo.deleteById(id);
    }

    // ------------------ PLAN SYSTEM ------------------
    public boolean canViewCandidateDetails(Hr hr) {
        if (hr.getPlanType() == null)
            return false;

        switch (hr.getPlanType()) {
            case "MONTHLY_UNLIMITED":
                if (hr.getPlanExpiryDate() == null || hr.getPlanExpiryDate().isBefore(LocalDateTime.now())) {
                    hr.setPlanType(null);
                    repo.save(hr);
                    return false;
                }
                return true;

            case "TEN_CANDIDATES":
            case "SINGLE_CANDIDATE":
                return hr.getRemainingViews() != null && hr.getRemainingViews() > 0;

            default:
                return false;
        }
    }

    public void consumeView(Hr hr) {
        if (hr.getPlanType() == null)
            return;

        if ("TEN_CANDIDATES".equals(hr.getPlanType()) ||
                "SINGLE_CANDIDATE".equals(hr.getPlanType())) {

            int remaining = hr.getRemainingViews() - 1;
            hr.setRemainingViews(remaining);

            if (remaining <= 0) {
                hr.setPlanType(null);
                hr.setRemainingViews(0);
            }

            repo.save(hr);
        }
    }

    public String getPlanDisplayName(Hr hr) {
        if (hr.getPlanType() == null)
            return "No Active Plan";

        return switch (hr.getPlanType()) {
            case "MONTHLY_UNLIMITED" -> "Monthly Unlimited Plan (Expires: " + hr.getPlanExpiryDate() + ")";
            case "TEN_CANDIDATES" -> "10 Candidates Plan (Remaining: " + hr.getRemainingViews() + ")";
            case "SINGLE_CANDIDATE" -> "Single Candidate Plan (Remaining: " + hr.getRemainingViews() + ")";
            default -> "Unknown Plan";
        };
    }
}
