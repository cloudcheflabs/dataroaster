package com.cloudcheflabs.dataroaster.operators.spark.crd;

import java.util.List;
import java.util.Map;

public class PodTemplate {

    private Affinity affinity;
    private List<Toleration> tolerations;
    private SecurityContext securityContext;
    private String schedulerName;
    private boolean hostNetwork = false;
    private Map<String, String> nodeSelector;
    private String priorityClassName = "";

    public String getPriorityClassName() {
        return priorityClassName;
    }

    public void setPriorityClassName(String priorityClassName) {
        this.priorityClassName = priorityClassName;
    }

    public Affinity getAffinity() {
        return affinity;
    }

    public void setAffinity(Affinity affinity) {
        this.affinity = affinity;
    }

    public List<Toleration> getTolerations() {
        return tolerations;
    }

    public void setTolerations(List<Toleration> tolerations) {
        this.tolerations = tolerations;
    }

    public SecurityContext getSecurityContext() {
        return securityContext;
    }

    public void setSecurityContext(SecurityContext securityContext) {
        this.securityContext = securityContext;
    }

    public String getSchedulerName() {
        return schedulerName;
    }

    public void setSchedulerName(String schedulerName) {
        this.schedulerName = schedulerName;
    }

    public boolean isHostNetwork() {
        return hostNetwork;
    }

    public void setHostNetwork(boolean hostNetwork) {
        this.hostNetwork = hostNetwork;
    }

    public Map<String, String> getNodeSelector() {
        return nodeSelector;
    }

    public void setNodeSelector(Map<String, String> nodeSelector) {
        this.nodeSelector = nodeSelector;
    }

    public static class SecurityContext {
        private int fsGroup;
        private int runAsGroup;
        private boolean runAsNonRoot;
        private int runAsUser;
        private SeLinuxOptions seLinuxOptions;
        private List<Integer> supplementalGroups;
        private List<Sysctl> sysctls;

        public int getFsGroup() {
            return fsGroup;
        }

        public void setFsGroup(int fsGroup) {
            this.fsGroup = fsGroup;
        }

        public int getRunAsGroup() {
            return runAsGroup;
        }

        public void setRunAsGroup(int runAsGroup) {
            this.runAsGroup = runAsGroup;
        }

        public boolean isRunAsNonRoot() {
            return runAsNonRoot;
        }

        public void setRunAsNonRoot(boolean runAsNonRoot) {
            this.runAsNonRoot = runAsNonRoot;
        }

        public int getRunAsUser() {
            return runAsUser;
        }

        public void setRunAsUser(int runAsUser) {
            this.runAsUser = runAsUser;
        }

        public SeLinuxOptions getSeLinuxOptions() {
            return seLinuxOptions;
        }

        public void setSeLinuxOptions(SeLinuxOptions seLinuxOptions) {
            this.seLinuxOptions = seLinuxOptions;
        }

        public List<Integer> getSupplementalGroups() {
            return supplementalGroups;
        }

        public void setSupplementalGroups(List<Integer> supplementalGroups) {
            this.supplementalGroups = supplementalGroups;
        }

        public List<Sysctl> getSysctls() {
            return sysctls;
        }

        public void setSysctls(List<Sysctl> sysctls) {
            this.sysctls = sysctls;
        }

        public static class WindowsOptions {
            private String gmsaCredentialSpec;
            private String gmsaCredentialSpecName;
            private String runAsUserName;

            public String getGmsaCredentialSpec() {
                return gmsaCredentialSpec;
            }

            public void setGmsaCredentialSpec(String gmsaCredentialSpec) {
                this.gmsaCredentialSpec = gmsaCredentialSpec;
            }

            public String getGmsaCredentialSpecName() {
                return gmsaCredentialSpecName;
            }

            public void setGmsaCredentialSpecName(String gmsaCredentialSpecName) {
                this.gmsaCredentialSpecName = gmsaCredentialSpecName;
            }

            public String getRunAsUserName() {
                return runAsUserName;
            }

            public void setRunAsUserName(String runAsUserName) {
                this.runAsUserName = runAsUserName;
            }
        }

        public static class Sysctl {
            private String name;
            private String value;

            public String getName() {
                return name;
            }

            public void setName(String name) {
                this.name = name;
            }

            public String getValue() {
                return value;
            }

            public void setValue(String value) {
                this.value = value;
            }
        }

        public static class SeLinuxOptions {
            private String level;
            private String role;
            private String type;
            private String user;

            public String getLevel() {
                return level;
            }

            public void setLevel(String level) {
                this.level = level;
            }

            public String getRole() {
                return role;
            }

            public void setRole(String role) {
                this.role = role;
            }

            public String getType() {
                return type;
            }

            public void setType(String type) {
                this.type = type;
            }

            public String getUser() {
                return user;
            }

            public void setUser(String user) {
                this.user = user;
            }
        }

    }
    public static class Toleration {
        private String effect;
        private String key;
        private String operator;
        private int tolerationSeconds;
        private String value;

        public String getEffect() {
            return effect;
        }

        public void setEffect(String effect) {
            this.effect = effect;
        }

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

        public int getTolerationSeconds() {
            return tolerationSeconds;
        }

        public void setTolerationSeconds(int tolerationSeconds) {
            this.tolerationSeconds = tolerationSeconds;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }
    }


    public static class Affinity {

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
}
