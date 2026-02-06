package me.huynhducphu.ping_me.repository.jpa.reels.spec;

import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Predicate;
import me.huynhducphu.ping_me.dto.request.reels.AdminReelFilterRequest;
import me.huynhducphu.ping_me.model.constant.ReelStatus;
import me.huynhducphu.ping_me.model.reels.Reel;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class ReelSpecifications {

    /**
     * Build a Specification that requires all tokens in the query to match either:
     * - a whole-word-like match in caption (case-insensitive), or
     * - an exact tag match inside the JSON `hashtags` column (we search for '"tag"' inside the JSON text).
     * <p>
     * This reduces false positives by requiring each token to be present.
     */
    public static Specification<Reel> searchByQuery(String q) {
        if (q == null || q.isBlank()) return null;

        String trimmed = q.trim().toLowerCase();
        String[] rawTokens = trimmed.split("\\s+");
        List<String> tokens = new ArrayList<>();
        for (String t : rawTokens) {
            if (t == null) continue;
            String tk = t.trim();
            if (tk.isEmpty()) continue;
            // strip leading hash if present
            if (tk.startsWith("#")) tk = tk.substring(1);
            tk = tk.trim();
            if (!tk.isEmpty()) tokens.add(tk);
        }

        if (tokens.isEmpty()) return null;

        return (root, query, cb) -> {
            List<Predicate> andPreds = new ArrayList<>();

            // lower-case expressions for caption and hashtags JSON text
            Expression<String> caption = cb.lower(cb.coalesce(root.get("caption"), ""));
            Expression<String> hashtags = cb.lower(cb.coalesce(root.get("hashtags"), ""));

            // create a wrapped caption with spaces to attempt word-like matching: ' ' || caption || ' '
            Expression<String> captionWrapped = cb.concat(cb.concat(" ", caption), " ");

            for (String tok : tokens) {
                // match caption as a separate word (heuristic) or hashtag JSON element
                String likePatternWord = "% " + tok + " %"; // match token surrounded by spaces in caption
                String likePatternTag = "%\"" + tok + "\"%"; // match "tag" inside JSON text

                Predicate pCaptionWord = cb.like(captionWrapped, likePatternWord);
                Predicate pHashtag = cb.like(hashtags, likePatternTag);

                // require either caption whole-word match OR exact hashtag match
                Predicate tokenMatch = cb.or(pCaptionWord, pHashtag);
                andPreds.add(tokenMatch);
            }

            return cb.and(andPreds.toArray(new Predicate[0]));
        };
    }

    // Specification for admin filtering (userId, caption, view range, date range, status)
    public static Specification<Reel> byFilter(AdminReelFilterRequest filter) {
        if (filter == null) return null;
        return (root, query, cb) -> {
            List<Predicate> preds = new ArrayList<>();

            if (filter.getUserId() != null) {
                preds.add(cb.equal(root.get("user").get("id"), filter.getUserId()));
            }

            if (filter.getCaption() != null && !filter.getCaption().isBlank()) {
                preds.add(cb.like(cb.lower(root.get("caption")), "%" + filter.getCaption().toLowerCase() + "%"));
            }

            if (filter.getMinViews() != null) {
                preds.add(cb.ge(root.get("viewCount"), filter.getMinViews()));
            }

            if (filter.getMaxViews() != null) {
                preds.add(cb.le(root.get("viewCount"), filter.getMaxViews()));
            }

            if (filter.getFrom() != null) {
                preds.add(cb.greaterThanOrEqualTo(root.get("createdAt"), filter.getFrom()));
            }

            if (filter.getTo() != null) {
                preds.add(cb.lessThanOrEqualTo(root.get("createdAt"), filter.getTo()));
            }

            if (filter.getStatus() != null && !filter.getStatus().isBlank()) {
                try {
                    ReelStatus rs = ReelStatus.valueOf(filter.getStatus().toUpperCase());
                    preds.add(cb.equal(root.get("status"), rs));
                } catch (Exception ignored) {
                    // ignore invalid status value
                }
            }

            if (preds.isEmpty()) return cb.conjunction();
            return cb.and(preds.toArray(new Predicate[0]));
        };
    }

}
