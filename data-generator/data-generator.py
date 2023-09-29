import string
import uuid
import random

import mysql.connector


def flat_map(f, xs):
    ys = []
    for x in xs:
        ys.extend(f(x))
    return ys


counter = 0


def get_counter():
    global counter
    counter += 1
    return counter


config = {
    "host": "localhost",
    "user": "root",
    "password": "toor",
    "database": "invoices"
}

invoice_sql = "INSERT INTO invoice (id, code, created_at, last_updated_at) " \
              "VALUES (%s, %s, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)"

invoices = [
    (str(uuid.uuid4()), ''.join(random.choice(string.ascii_letters) for i in range(16))),
    (str(uuid.uuid4()), ''.join(random.choice(string.ascii_letters) for i in range(16)))
]

invoice_item_sql = "INSERT INTO invoice_item (id, invoice_id, name) " \
                   "VALUES (%s, %s, %s)"

invoice_items = flat_map(
    lambda x: [
        (str(uuid.uuid4()), x[0], "Product %s" % get_counter()),
        (str(uuid.uuid4()), x[0], "Product %s" % get_counter()),
    ],
    invoices
)

connection = mysql.connector.connect(**config)
cursor = connection.cursor()

try:
    print("Inserting data...")
    print(invoices)
    print(invoice_items)

    cursor.executemany(invoice_sql, invoices)
    cursor.executemany(invoice_item_sql, invoice_items)

    connection.commit()
except mysql.connector.Error as err:
    print(f"Error: {err}")
finally:
    cursor.close()
    connection.close()