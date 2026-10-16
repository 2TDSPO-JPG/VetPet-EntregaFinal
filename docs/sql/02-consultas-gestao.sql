SELECT p.pet_code, p.nome, MAX(e.data_hora) AS ultimo_atendimento
  FROM VP_PET p
  LEFT JOIN VP_EXAME e ON e.pet_id = p.id AND e.status = 'REALIZADO'
 GROUP BY p.pet_code, p.nome
HAVING MAX(e.data_hora) IS NULL OR MAX(e.data_hora) < ADD_MONTHS(SYSTIMESTAMP, -12)
 ORDER BY ultimo_atendimento NULLS FIRST;

SELECT v.nome AS veterinario, e.data_hora, e.tipo, p.nome AS pet, e.status
  FROM VP_EXAME e
  JOIN VP_VETERINARIO v ON v.id = e.veterinario_id
  JOIN VP_PET p ON p.id = e.pet_id
 WHERE TRUNC(e.data_hora) = TRUNC(SYSDATE)
 ORDER BY v.nome, e.data_hora;

SELECT tipo,
       COUNT(*) AS total,
       SUM(CASE WHEN status = 'CANCELADO' THEN 1 ELSE 0 END) AS cancelados,
       ROUND(100 * SUM(CASE WHEN status = 'CANCELADO' THEN 1 ELSE 0 END) / COUNT(*), 1) AS percentual
  FROM VP_EXAME
 GROUP BY tipo
 ORDER BY percentual DESC;

SELECT p.nome, e.data_hora, pr.peso_aferido_kg
  FROM VP_PRONTUARIO pr
  JOIN VP_EXAME e ON e.id = pr.exame_id
  JOIN VP_PET p ON p.id = e.pet_id
 WHERE p.pet_code = :pet_code
   AND pr.peso_aferido_kg IS NOT NULL
 ORDER BY e.data_hora;

SELECT ROUND(AVG(EXTRACT(DAY FROM (respondida_em - criada_em)) * 24
               + EXTRACT(HOUR FROM (respondida_em - criada_em))), 1) AS horas_media
  FROM VP_PERGUNTA
 WHERE respondida_em IS NOT NULL;
