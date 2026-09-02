package ba.equipment.weapon;

import ba.equipment.armor.DefenseType;
import javabase.BinaryTuple;

public enum AttackType {
	/**
	 * 普通
	 */
	Normal(),

	/**
	 * 爆发
	 */
	@SuppressWarnings("unchecked")
	Explosion(BinaryTuple.of(DefenseType.LightArmor, AttackDamageEffect.Weak), BinaryTuple.of(DefenseType.SpecialArmor, AttackDamageEffect.Resist), BinaryTuple.of(DefenseType.ElasticArmor, AttackDamageEffect.Resist)),

	/**
	 * 贯穿
	 */
	@SuppressWarnings("unchecked")
	Penetration(BinaryTuple.of(DefenseType.LightArmor, AttackDamageEffect.Resist), BinaryTuple.of(DefenseType.HeavyArmor, AttackDamageEffect.Weak)),

	/**
	 * 神秘
	 */
	@SuppressWarnings("unchecked")
	Mystic(BinaryTuple.of(DefenseType.HeavyArmor, AttackDamageEffect.Resist), BinaryTuple.of(DefenseType.SpecialArmor, AttackDamageEffect.Weak)),

	/**
	 * 振动
	 */
	@SuppressWarnings("unchecked")
	Sonic(BinaryTuple.of(DefenseType.HeavyArmor, AttackDamageEffect.Resist), BinaryTuple.of(DefenseType.SpecialArmor, AttackDamageEffect.Effective), BinaryTuple.of(DefenseType.ElasticArmor, AttackDamageEffect.Weak)),

	/**
	 * 攻城，目前没有学生是该攻击类型
	 */
	Siege;

	/**
	 * 克制关系表
	 */
	private final AttackDamageEffect[] restraintMap;

	/**
	 * 不指定克制关系则默认全是Normal伤害
	 */
	private AttackType() {
		DefenseType[] defTypes = DefenseType.values();
		restraintMap = new AttackDamageEffect[defTypes.length];
		for (DefenseType defType : defTypes)
			restraintMap[defType.id] = AttackDamageEffect.Normal;
	}

	/**
	 * 设置克制关系，传入装甲类型和克制类型
	 * 
	 * @param tuples
	 */
	@SuppressWarnings("unchecked")
	private AttackType(BinaryTuple<DefenseType, AttackDamageEffect>... tuples) {
		this();
		for (BinaryTuple<DefenseType, AttackDamageEffect> tuple : tuples)
			restraintMap[tuple.val1.id] = tuple.val2;
	}

	/**
	 * 根据传入的防御类型决定克制效果
	 * 
	 * @param defType
	 * @return
	 */
	public AttackDamageEffect resolveDamageEffect(DefenseType defType) {
		return restraintMap[defType.id];
	}

	/**
	 * 根据传入的防御类型决定克制造成的伤害比例
	 * 
	 * @param defType
	 * @return
	 */
	public float resolveDamageDealt(DefenseType defType) {
		return resolveDamageEffect(defType).damageDealt;
	}
}
