package com.arthurchenevans.arsbotania.core.divine;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;

/** What Divine pieces an entity has in its armor slots right now. */
public final class DivineLoadout {
    private final Map<ArmorSlot, DivinePiece> worn;

    private DivineLoadout(Map<ArmorSlot, DivinePiece> worn) {
        this.worn = Collections.unmodifiableMap(new EnumMap<>(worn));
    }

    public static Builder builder() {
        return new Builder();
    }

    public Optional<DivinePiece> in(ArmorSlot slot) {
        return Optional.ofNullable(worn.get(slot));
    }

    public int count() {
        return worn.size();
    }

    public boolean isFullSet() {
        return worn.size() == ArmorSlot.values().length;
    }

    public Iterable<DivinePiece> pieces() {
        return worn.values();
    }

    public static final class Builder {
        private final Map<ArmorSlot, DivinePiece> worn = new EnumMap<>(ArmorSlot.class);

        public Builder wear(DivinePiece piece) {
            return wear(piece.slot(), piece);
        }

        public Builder wear(ArmorSlot slot, DivinePiece piece) {
            if (piece.slot() != slot) {
                throw new IllegalArgumentException(piece.displayName() + " cannot be worn in " + slot);
            }
            worn.put(slot, piece);
            return this;
        }

        public DivineLoadout build() {
            return new DivineLoadout(worn);
        }
    }
}
