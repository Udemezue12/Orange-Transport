package com.astrotech.transport.jobrunr.taskScheduler;

import com.astrotech.transport.configProperties.PingerProperties;

import com.astrotech.transport.events.*;
import com.astrotech.transport.jobrunr.orchestration.JobRunrManagementService;
import com.astrotech.transport.jobrunr.tasks.*;
import com.astrotech.transport.service.BookingSessionService;
import com.astrotech.transport.service.GenerateRegisterCodeService;
import com.astrotech.transport.service.TerminalService;
import lombok.RequiredArgsConstructor;
import org.jobrunr.scheduling.JobScheduler;
import org.jobrunr.scheduling.cron.Cron;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import static com.astrotech.transport.core.TrimWhiteSpace.sanitize;

@RequiredArgsConstructor
@Configuration
public class TaskScheduler {
    private final JobScheduler jobScheduler;
    private final DeleteBlacklistedTokenTask cleanupToken;
    private final PingerProperties properties;
    private final PingUrlTask pingUrlTask;
    private final LicenseVerificationTask licenseVerificationTask;
    private final ProfileUploadTask profileUploadTask;
    private final DriverProfileUploadTask driverProfileUploadTask;
    private final VehicleImageUploadTask vehicleImageUploadTask;
    private final SeatTask seatTask;
    private final PaymentNotificationTask notificationTask;
    private final GenerateTicketTask generateTicketTask;
    private final TicketPdfTask ticketPdfTask;
    private final BookingSessionService bookingSessionService;
    private final CreateTransloadingTask transloadingTask;
    private final GenerateRegisterCodeService codeService;
    private final JobRunrManagementService managementService;
    private final TerminalService terminalService;

    @EventListener(ApplicationReadyEvent.class)
    public void scheduleRecurringJobs() {
        jobScheduler.scheduleRecurrently(
                "expire-pending-booking-sessions",
                "*/10 * * * *",
                bookingSessionService::expirePendingSessionsOlderThanTenMinutes
        );
    }

    @EventListener(ApplicationReadyEvent.class)
    public void deleteSuccessfulJobs() {
        jobScheduler.scheduleRecurrently(
                "purge-successful-jobs",
                "0 0 */6 * *",
                managementService::purgeSucceededJobs
        );
        jobScheduler.scheduleRecurrently(
                "purge-deleted-jobs",
                "0 0 */6 * *",
                managementService::purgeDeletedJobs);

    }

    @EventListener(ApplicationReadyEvent.class)
    public void getTerminals() {
        jobScheduler.scheduleRecurrently(
                "get-terminals",
                "0 0 */4 * *",
                () -> terminalService.getAllTerminals(
                        0,
                        15,
                        "name",
                        true)
        );
    }

    @EventListener(ApplicationReadyEvent.class)
    public void deletePdfs() {
        jobScheduler.scheduleRecurrently(
                "purge-expired-ticket-pdfs",
                Cron.daily(),
                // "0 */2 * * * *",
                ticketPdfTask::purgeExpiredTicketPdfsJob
        );
    }

    @EventListener(ApplicationReadyEvent.class)
    public void deleteCodes() {
        jobScheduler.scheduleRecurrently(
                "purge-invalid-codes",
                "0 */3 * * * *",
                codeService::deleteInvalidCodes

        );

    }

    @EventListener(ApplicationReadyEvent.class)
    public void deleteTokens() {
        jobScheduler.scheduleRecurrently(
                "revoked-token-cleanup",
                Cron.weekly(),
                // "0 */2 * * * *",
                cleanupToken::cleanupExpiredTokens);
    }

    @EventListener(ApplicationReadyEvent.class)
    public void pingUrls() {

        properties.pingUrls().forEach(site ->
                jobScheduler.scheduleRecurrently(
                        "ping-" + sanitize(site),
                        "*/8 * * * *",
                        () -> pingUrlTask.pingUrl(site)
                )
        );
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void verify(LicenseVerificationEvent event) {
        var licenseNumber = event.licenseNumber();
        var userId = event.userId();
        jobScheduler.enqueue(
                () -> licenseVerificationTask.verifyDriverLicense(userId, licenseNumber)
        );

    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void driverProfileUpload(ImageUploadRequest event) {
        var assetId = event.assetId();
        var publicId = event.publicId();
        var userId = event.Id();
        jobScheduler.enqueue(
                () -> driverProfileUploadTask.uploadProfileImage(userId, publicId, assetId)
        );

    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void driverProfileUpdate(ImageUpdateRequest event) {
        var oldPublicId = event.oldPublicId();
        var oldResourceType = event.oldResourceType();
        var userId = event.Id();
        var newAssetId = event.newAssetId();
        var newPublicId = event.newPublicId();
        jobScheduler.enqueue(
                () -> driverProfileUploadTask.updateProfileImage(userId, oldPublicId, oldResourceType, newAssetId, newPublicId)
        );
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void profileUpload(ProfilePicUploadEvent event) {
        var assetId = event.assetId();
        var publicId = event.publicId();
        var currentUserId = event.userId();
        jobScheduler.enqueue(
                () -> profileUploadTask.uploadProfilePic(String.valueOf(currentUserId), publicId, assetId)
        );

    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void profilePicUpdate(ProfilePicUpdateRequest event) {
        var oldPublicId = event.oldPublicId();
        var oldResourceType = event.oldResourceType();
        var userId = event.Id();
        var newAssetId = event.newAssetId();
        var newPublicId = event.newPublicId();
        jobScheduler.enqueue(
                () -> profileUploadTask.updateProfilePic(userId, oldPublicId, oldResourceType, newAssetId, newPublicId)
        );
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void profileDocumentUpload(DocumentUploadEvent event) {
        var publicId = event.publicId();
        var currentUserId = event.userId();
        var assetId = event.assetId();
        var documentType = event.documentType();
        var documentNumber = event.documentNumber();

        jobScheduler.enqueue(
                () -> profileUploadTask.saveDocument(
                        currentUserId,
                        documentType,
                        documentNumber,
                        assetId,
                        publicId
                )
        );
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void vehicleImageUpload(VehicleImageUploadRequest event) {
        var assetId = event.assetId();
        var publicId = event.publicId();
        var vehicleId = event.Id();
        jobScheduler.enqueue(
                () -> vehicleImageUploadTask.uploadVehicleImage(vehicleId, publicId, assetId)
        );

    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void vehicleImageUpdate(VehicleImageUpdateRequest event) {
        var vehicleId = event.vehicleId();
        var replacements = event.replacements();
        jobScheduler.enqueue(
                () -> vehicleImageUploadTask.updateVehicleImages(
                        vehicleId,
                        replacements
                )
        );
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void createSeat(CreateSeat event) {
        var vehicleId = event.vehicleId();
        var capacity = event.capacity();
        var seatsPerRow = event.seatsPerRow();
        jobScheduler.enqueue(
                () -> seatTask.create(String.valueOf(vehicleId), capacity, seatsPerRow)
        );
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void updateSeat(UpdateVehicleSeat event) {
        var vehicleId = event.vehicleId();
        var capacity = event.vehicleCapacity();
        var seatsPerRow = event.seatsPerRow();
        jobScheduler.enqueue(
                () -> seatTask.update(vehicleId, capacity, seatsPerRow)
        );
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(
            PaymentSuccessEvent event) {
        var bookingCode = event.bookingCode();
        var name = event.name();
        var email = event.email();
        var paymentId = event.paymentId();
        var phoneNumber = event.phoneNumber();
        jobScheduler.enqueue(
                () -> notificationTask.sendPaymentSuccessNotificationEmail(
                        bookingCode,
                        name,
                        email,
                        paymentId));
        jobScheduler.enqueue(
                () -> notificationTask.sendPaymentSuccessNotificationSms(
                        phoneNumber,
                        name,
                        bookingCode

                ));
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void generate(GenerateTicketEvent event) {
        var paymentId = event.paymentId();
        jobScheduler.enqueue(() -> generateTicketTask.generateTicket(paymentId));

    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void createTransloading(CreateTransloadingEvent event) {
        var id = event.id();
        var incidentReason = event.incidentReason();
        var incidentDescription = event.incidentReason();
        var location = event.locationName();
        jobScheduler.enqueue(() -> transloadingTask.create(id, incidentReason, incidentDescription, location));
    }


}
