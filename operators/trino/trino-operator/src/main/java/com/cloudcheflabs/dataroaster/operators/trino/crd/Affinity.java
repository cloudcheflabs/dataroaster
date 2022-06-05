package com.cloudcheflabs.dataroaster.operators.trino.crd;

import java.util.List;
import java.util.Map;

public class Affinity {
    private NodeAffinity nodeAffinity;
    private PodAffinity podAffinity;
    private PodAntiAffinity podAntiAffinity;

    public NodeAffinity getNodeAffinity() {
        return nodeAffinity;
    }

    public void setNodeAffinity(NodeAffinity nodeAffinity) {
        this.nodeAffinity = nodeAffinity;
    }

    public PodAffinity getPodAffinity() {
        return podAffinity;
    }

    public void setPodAffinity(PodAffinity podAffinity) {
        this.podAffinity = podAffinity;
    }

    public PodAntiAffinity getPodAntiAffinity() {
        return podAntiAffinity;
    }

    public void setPodAntiAffinity(PodAntiAffinity podAntiAffinity) {
        this.podAntiAffinity = podAntiAffinity;
    }

    public static class MatchExpression {
        private String key;
        private String operator;
        private List<String> values;

        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }

        public String getOperator() {
            return operator;
        }

        public void setOperator(String operator) {
            this.operator = operator;
        }

        public List<String> getValues() {
            return values;
        }

        public void setValues(List<String> values) {
            this.values = values;
        }
    }
    public static class MatchField extends MatchExpression{
    }

    public static class NodeAffinity {
        private List<PreferredDuringSchedulingIgnoredDuringExecution> preferredDuringSchedulingIgnoredDuringExecution;
        private RequiredDuringSchedulingIgnoredDuringExecution requiredDuringSchedulingIgnoredDuringExecution;

        public List<PreferredDuringSchedulingIgnoredDuringExecution> getPreferredDuringSchedulingIgnoredDuringExecution() {
            return preferredDuringSchedulingIgnoredDuringExecution;
        }

        public void setPreferredDuringSchedulingIgnoredDuringExecution(List<PreferredDuringSchedulingIgnoredDuringExecution> preferredDuringSchedulingIgnoredDuringExecution) {
            this.preferredDuringSchedulingIgnoredDuringExecution = preferredDuringSchedulingIgnoredDuringExecution;
        }

        public RequiredDuringSchedulingIgnoredDuringExecution getRequiredDuringSchedulingIgnoredDuringExecution() {
            return requiredDuringSchedulingIgnoredDuringExecution;
        }

        public void setRequiredDuringSchedulingIgnoredDuringExecution(RequiredDuringSchedulingIgnoredDuringExecution requiredDuringSchedulingIgnoredDuringExecution) {
            this.requiredDuringSchedulingIgnoredDuringExecution = requiredDuringSchedulingIgnoredDuringExecution;
        }

        public static class RequiredDuringSchedulingIgnoredDuringExecution {
            private List<NodeSelectorTerm> nodeSelectorTerms;

            public List<NodeSelectorTerm> getNodeSelectorTerms() {
                return nodeSelectorTerms;
            }

            public void setNodeSelectorTerms(List<NodeSelectorTerm> nodeSelectorTerms) {
                this.nodeSelectorTerms = nodeSelectorTerms;
            }

            public static class NodeSelectorTerm {
                private List<MatchExpression> matchExpressions;
                private List<MatchField> matchFields;

                public List<MatchExpression> getMatchExpressions() {
                    return matchExpressions;
                }

                public void setMatchExpressions(List<MatchExpression> matchExpressions) {
                    this.matchExpressions = matchExpressions;
                }

                public List<MatchField> getMatchFields() {
                    return matchFields;
                }

                public void setMatchFields(List<MatchField> matchFields) {
                    this.matchFields = matchFields;
                }
            }
        }

        public static class PreferredDuringSchedulingIgnoredDuringExecution {
            private Preference preference;
            private int weight;

            public Preference getPreference() {
                return preference;
            }

            public void setPreference(Preference preference) {
                this.preference = preference;
            }

            public int getWeight() {
                return weight;
            }

            public void setWeight(int weight) {
                this.weight = weight;
            }

            public static class Preference {
                private List<MatchExpression> matchExpressions;
                private List<MatchField> matchFields;

                public List<MatchExpression> getMatchExpressions() {
                    return matchExpressions;
                }

                public void setMatchExpressions(List<MatchExpression> matchExpressions) {
                    this.matchExpressions = matchExpressions;
                }

                public List<MatchField> getMatchFields() {
                    return matchFields;
                }

                public void setMatchFields(List<MatchField> matchFields) {
                    this.matchFields = matchFields;
                }
            }
        }
    }

    public static class PodAffinity {

        private List<PreferredDuringSchedulingIgnoredDuringExecution> preferredDuringSchedulingIgnoredDuringExecution;
        private List<RequiredDuringSchedulingIgnoredDuringExecution> requiredDuringSchedulingIgnoredDuringExecution;

        public List<PreferredDuringSchedulingIgnoredDuringExecution> getPreferredDuringSchedulingIgnoredDuringExecution() {
            return preferredDuringSchedulingIgnoredDuringExecution;
        }

        public void setPreferredDuringSchedulingIgnoredDuringExecution(List<PreferredDuringSchedulingIgnoredDuringExecution> preferredDuringSchedulingIgnoredDuringExecution) {
            this.preferredDuringSchedulingIgnoredDuringExecution = preferredDuringSchedulingIgnoredDuringExecution;
        }

        public List<RequiredDuringSchedulingIgnoredDuringExecution> getRequiredDuringSchedulingIgnoredDuringExecution() {
            return requiredDuringSchedulingIgnoredDuringExecution;
        }

        public void setRequiredDuringSchedulingIgnoredDuringExecution(List<RequiredDuringSchedulingIgnoredDuringExecution> requiredDuringSchedulingIgnoredDuringExecution) {
            this.requiredDuringSchedulingIgnoredDuringExecution = requiredDuringSchedulingIgnoredDuringExecution;
        }

        public static class RequiredDuringSchedulingIgnoredDuringExecution {
            private LabelSelector labelSelector;
            private List<String> namespaces;
            private String topologyKey;

            public LabelSelector getLabelSelector() {
                return labelSelector;
            }

            public void setLabelSelector(LabelSelector labelSelector) {
                this.labelSelector = labelSelector;
            }

            public List<String> getNamespaces() {
                return namespaces;
            }

            public void setNamespaces(List<String> namespaces) {
                this.namespaces = namespaces;
            }

            public String getTopologyKey() {
                return topologyKey;
            }

            public void setTopologyKey(String topologyKey) {
                this.topologyKey = topologyKey;
            }
        }

        public static class LabelSelector {
            private List<MatchExpression> matchExpressions;
            private Map<String, String> matchLabels;

            public List<MatchExpression> getMatchExpressions() {
                return matchExpressions;
            }

            public void setMatchExpressions(List<MatchExpression> matchExpressions) {
                this.matchExpressions = matchExpressions;
            }

            public Map<String, String> getMatchLabels() {
                return matchLabels;
            }

            public void setMatchLabels(Map<String, String> matchLabels) {
                this.matchLabels = matchLabels;
            }
        }

        public static class PreferredDuringSchedulingIgnoredDuringExecution {
            private PodAffinityTerm podAffinityTerm;
            private int weight;

            public PodAffinityTerm getPodAffinityTerm() {
                return podAffinityTerm;
            }

            public void setPodAffinityTerm(PodAffinityTerm podAffinityTerm) {
                this.podAffinityTerm = podAffinityTerm;
            }

            public int getWeight() {
                return weight;
            }

            public void setWeight(int weight) {
                this.weight = weight;
            }

            public static class PodAffinityTerm {
                private LabelSelector labelSelector;
                private List<String> namespaces;
                private String topologyKey;

                public LabelSelector getLabelSelector() {
                    return labelSelector;
                }

                public void setLabelSelector(LabelSelector labelSelector) {
                    this.labelSelector = labelSelector;
                }

                public List<String> getNamespaces() {
                    return namespaces;
                }

                public void setNamespaces(List<String> namespaces) {
                    this.namespaces = namespaces;
                }

                public String getTopologyKey() {
                    return topologyKey;
                }

                public void setTopologyKey(String topologyKey) {
                    this.topologyKey = topologyKey;
                }


            }
        }

    }

    public static class PodAntiAffinity extends PodAffinity{

    }
}
