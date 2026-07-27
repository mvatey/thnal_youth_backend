//package org.example.tnal_youth_backend.donation.sponsordonation.repository;
//
//import org.example.tnal_youth_backend.donation.sponsordonation.entity.SponsorDonation;
//import org.springframework.data.jpa.repository.JpaRepository;
//import org.springframework.data.jpa.repository.Query;
//import org.springframework.data.repository.query.Param;
//
//import java.util.List;
//import java.util.Optional;
//
//public interface SponsorDonationRepository
//        extends JpaRepository<SponsorDonation, Long> {
//
//    List<SponsorDonation>
//    findAllByOrderByPaidAtDescIdDesc();
//
//    List<SponsorDonation>
//    findAllByPaymentMethodIdOrderByPaidAtDescIdDesc(
//            Short paymentMethodId
//    );
//
//    @Query(
//            value = """
//                    SELECT sponsor_donation.*
//                    FROM sponsor_donations sponsor_donation
//
//                    INNER JOIN sponsors sponsor
//                            ON sponsor.id =
//                               sponsor_donation.sponsor_id
//
//                    WHERE LOWER(sponsor.name)
//                          LIKE LOWER(
//                              CONCAT(
//                                  '%',
//                                  :search,
//                                  '%'
//                              )
//                          )
//
//                       OR LOWER(
//                              COALESCE(
//                                  sponsor.phone,
//                                  ''
//                              )
//                          )
//                          LIKE LOWER(
//                              CONCAT(
//                                  '%',
//                                  :search,
//                                  '%'
//                              )
//                          )
//
//                       OR LOWER(
//                              COALESCE(
//                                  sponsor.email::TEXT,
//                                  ''
//                              )
//                          )
//                          LIKE LOWER(
//                              CONCAT(
//                                  '%',
//                                  :search,
//                                  '%'
//                              )
//                          )
//
//                    ORDER BY
//                        sponsor_donation.paid_at DESC,
//                        sponsor_donation.id DESC
//                    """,
//            nativeQuery = true
//    )
//    List<SponsorDonation> searchBySponsor(
//            @Param("search")
//            String search
//    );
//
//    @Query(
//            value = """
//                    SELECT sponsor_donation_no
//                    FROM sponsor_donations
//                    WHERE sponsor_donation_no
//                          LIKE CONCAT(:prefix, '%')
//                    ORDER BY sponsor_donation_no DESC
//                    LIMIT 1
//                    """,
//            nativeQuery = true
//    )
//    Optional<String> findLatestNumberByPrefix(
//            @Param("prefix")
//            String prefix
//    );
//
//    long countBySponsorId(
//            Long sponsorId
//    );
//}
