SELECT id, nome, email, telefone
FROM VP_TUTOR
ORDER BY id DESC;
SELECT p.id, p.pet_code, p.nome, p.especie, p.raca, p.peso_kg, t.nome AS tutor
FROM VP_PET p
JOIN VP_TUTOR_PET tp ON tp.pet_id = p.id
JOIN VP_TUTOR t ON t.id = tp.tutor_id
ORDER BY p.id DESC;