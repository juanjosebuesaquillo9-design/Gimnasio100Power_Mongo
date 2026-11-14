// BASE DE DATOS NOSQL - GIMNASIO 100% POWER (MONGODB)
// Arquitectura Híbrida: Documentos Embebidos + Documentos Referenciados

use("gimnasio_100_power_nosql");

// 1. VALIDACIÓN DE ESQUEMA ($jsonSchema) EN USUARIOS_STAFF
db.runCommand({
  collMod: "usuarios_staff",
  validator: {
    $jsonSchema: {
      bsonType: "object",
      required: ["correo", "password_hash", "rol", "nombre_mostrar", "activo"],
      properties: {
        correo: { bsonType: "string" },
        password_hash: { bsonType: "string" },
        rol: { enum: ["ADMIN", "RECEPCION", "ENTRENADOR"] },
        nombre_mostrar: { bsonType: "string" },
        activo: { bsonType: "bool" }
      }
    }
  }
});

// 2. CREACIÓN DE ÍNDICES PARA OPTIMIZACIÓN DE CONSULTAS
db.usuarios_staff.createIndex({ correo: 1 }, { unique: true });
db.clientes.createIndex({ cedula: 1 }, { unique: true });
db.clientes.createIndex({ "membresia.activa": 1, "membresia.fecha_vencimiento": 1 });
db.asistencias.createIndex({ cliente_id: 1, fecha_hora: -1 });
db.asistencias.createIndex({ fecha_hora: -1 });

// 3. CONSULTAS CRUD DE VERIFICACIÓN
// 3.1 Conteo de documentos por colección (21.010 documentos totales)
print("Staff:", db.usuarios_staff.countDocuments());
print("Clientes:", db.clientes.countDocuments());
print("Asistencias:", db.asistencias.countDocuments());

// 3.2 Consulta sobre subdocumentos embebidos (membresía activa + rutina)
db.clientes.find(
  { "membresia.activa": true, "rutina_activa": { $ne: null } },
  { nombre_completo: 1, cedula: 1, "membresia.tipo": 1, "rutina_activa.objetivo": 1, _id: 0 }
).limit(10);

// 3.3 Actualización embebida: Registrar nuevo pago en el arreglo historial_pagos
db.clientes.updateOne(
  { cedula: "1143325454" },
  {
    $push: {
      historial_pagos: {
        id_pago: "PAG-DEMO-001",
        fecha_pago: new Date(),
        plan_pagado: "Mensual",
        metodo: "Nequi",
        monto: 75000
      }
    },
    $set: {
      "membresia.activa": true,
      "membresia.tipo": "Mensual"
    }
  }
);

// 4. CONSULTAS DE AGREGACIÓN (AGGREGATION PIPELINES)
// 4.1 Ingresos totales y transacciones agrupadas por plan (descomponiendo arreglo embebido)
db.clientes.aggregate([
  { $unwind: "$historial_pagos" },
  {
    $group: {
      _id: "$historial_pagos.plan_pagado",
      total_transacciones: { $sum: 1 },
      ingresos_recaudados: { $sum: "$historial_pagos.monto" }
    }
  },
  { $sort: { ingresos_recaudados: -1 } }
]);

// 4.2 Cruce referencial ($lookup) entre asistencias y clientes
db.asistencias.aggregate([
  { $match: { estado_acceso: "AUTORIZADO" } },
  { $sort: { fecha_hora: -1 } },
  { $limit: 10 },
  {
    $lookup: {
      from: "clientes",
      localField: "cliente_id",
      foreignField: "_id",
      as: "afiliado"
    }
  },
  { $unwind: "$afiliado" },
  {
    $project: {
      _id: 0,
      fecha_hora: 1,
      torniquete: 1,
      nombre_cliente: "$afiliado.nombre_completo",
      cedula: "$afiliado.cedula",
      plan: "$afiliado.membresia.tipo"
    }
  }
]);