package com.sportlink.matchingservice.service;

import com.sportlink.matchingservice.dto.UserProfileDto;
import com.sportlink.matchingservice.model.SuggestedUser;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;

/**
 * Stateless component that computes compatibility scores between two user profiles.
 *
 * Scoring dimensions and weights (configurable in application.properties):
 *   sport    — do their sport preferences overlap?
 *   skill    — how close are their skill levels?
 *   location — do they share city and/or district?
 *   rating   — what is the candidate's reputation score?
 *   language — do they speak the same language?
 *
 * Each dimension returns a value in [0.0, 1.0].
 * The final compatibilityScore is the weighted sum, also in [0.0, 1.0].
 */
@Component
public class CompatibilityScorer {

    // Skill level order used to compute proximity
    private static final List<String> SKILL_ORDER =
            List.of("BEGINNER", "INTERMEDIATE", "ADVANCED", "PROFESSIONAL");

    // Rating score expected range from Rating Management Service (0–5)
    private static final double MAX_RATING = 5.0;

    private final double weightSport;
    private final double weightSkill;
    private final double weightLocation;
    private final double weightRating;
    private final double weightLanguage;

    public CompatibilityScorer(
            @Value("${sportlink.matching.weights.sport:0.35}") double weightSport,
            @Value("${sportlink.matching.weights.skill:0.25}") double weightSkill,
            @Value("${sportlink.matching.weights.location:0.20}") double weightLocation,
            @Value("${sportlink.matching.weights.rating:0.15}") double weightRating,
            @Value("${sportlink.matching.weights.language:0.05}") double weightLanguage) {
        this.weightSport = weightSport;
        this.weightSkill = weightSkill;
        this.weightLocation = weightLocation;
        this.weightRating = weightRating;
        this.weightLanguage = weightLanguage;
    }

    /**
     * Compute a full SuggestedUser value object for {@code candidate} as seen from
     * {@code requester}'s perspective.
     *
     * @param requester the user requesting suggestions
     * @param candidate a candidate profile retrieved from the Search Service
     * @param candidateRating the candidate's aggregated reputation score (0–5),
     *                        or 0.0 if unavailable
     * @param scheduleOverlap pre-resolved schedule overlap score (0–1) from Scheduling
     *                        Service, or a neutral 0.5 when the service is stubbed
     */
    public SuggestedUser score(
            UserProfileDto requester,
            UserProfileDto candidate,
            double candidateRating,
            double scheduleOverlap) {

        double sport    = scoreSport(requester.getSportPreferences(), candidate.getSportPreferences());
        double skill    = scoreSkill(requester.getSkillLevel(), candidate.getSkillLevel());
        double location = scoreLocation(requester.getCity(), requester.getDistrict(),
                                        candidate.getCity(), candidate.getDistrict());
        double rating   = scoreRating(candidateRating);
        double language = scoreLanguage(requester.getLanguage(), candidate.getLanguage());

        // Schedule overlap partially replaces/enhances the location component
        // We blend it in: final location contribution = location * 0.6 + schedule * 0.4
        double blendedLocation = location * 0.6 + scheduleOverlap * 0.4;

        double total = sport    * weightSport
                     + skill    * weightSkill
                     + blendedLocation * weightLocation
                     + rating   * weightRating
                     + language * weightLanguage;

        return SuggestedUser.builder()
                .userId(candidate.getUserId())
                .compatibilityScore(round(total))
                .sportScore(round(sport))
                .skillScore(round(skill))
                .locationScore(round(blendedLocation))
                .ratingScore(round(rating))
                .languageScore(round(language))
                .build();
    }

    // -------------------------------------------------------------------------
    // Individual dimension scorers
    // -------------------------------------------------------------------------

    /**
     * Sport affinity: Jaccard similarity of sport preference sets.
     * 1.0 = identical sets, 0.0 = completely disjoint.
     */
    double scoreSport(List<String> a, List<String> b) {
        if (isNullOrEmpty(a) || isNullOrEmpty(b)) return 0.0;
        long intersection = a.stream().filter(b::contains).count();
        long union = a.stream().filter(s -> !b.contains(s)).count() + b.size();
        return union == 0 ? 0.0 : (double) intersection / union;
    }

    /**
     * Skill proximity: 1.0 if same level, decays by 1/3 per level apart.
     */
    double scoreSkill(String a, String b) {
        if (a == null || b == null) return 0.0;
        int ia = SKILL_ORDER.indexOf(a.toUpperCase());
        int ib = SKILL_ORDER.indexOf(b.toUpperCase());
        if (ia == -1 || ib == -1) return 0.0;
        int distance = Math.abs(ia - ib);
        return Math.max(0.0, 1.0 - distance / 3.0);
    }

    /**
     * Location match:
     *   1.0 — same district
     *   0.6 — same city, different district
     *   0.0 — different city
     */
    double scoreLocation(String cityA, String districtA, String cityB, String districtB) {
        if (cityA == null || cityB == null) return 0.0;
        if (!cityA.equalsIgnoreCase(cityB)) return 0.0;
        if (districtA != null && districtB != null && districtA.equalsIgnoreCase(districtB)) return 1.0;
        return 0.6;
    }

    /**
     * Rating score: normalised to [0, 1] from a 0–5 scale.
     * A new user with no ratings gets a neutral 0.5.
     */
    double scoreRating(double rating) {
        if (rating <= 0.0) return 0.5; // neutral for unrated users
        return Math.min(1.0, rating / MAX_RATING);
    }

    /**
     * Language match: 1.0 if same language, 0.0 otherwise.
     */
    double scoreLanguage(String a, String b) {
        if (a == null || b == null) return 0.0;
        return a.equalsIgnoreCase(b) ? 1.0 : 0.0;
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private boolean isNullOrEmpty(Collection<?> c) {
        return c == null || c.isEmpty();
    }

    private double round(double value) {
        return Math.round(value * 1000.0) / 1000.0;
    }
}
