package tn.atb.backend.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * One feature's contribution to a credit file's AI decision, stored alongside the
 * score so the agent can still see the reasoning later without re-running the analysis.
 *
 * {@code impact} is the feature's weight in the model's log-odds: positive lowers the
 * risk, negative raises it. {@code feature} holds the model's own column name; turning
 * it into wording an agent reads is the frontend's job.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DecisionFactor {

    private String feature;

    private Double impact;

    private Boolean reducesRisk;
}
