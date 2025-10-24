import streamlit as st
import hashlib
from datetime import datetime
import json
from io import StringIO
import os
import csv
import uuid
import html

# ----- Config -----
DATA_FILE = "blockchain_data.json"
st.set_page_config(page_title="Blockchain Transaction Manager", layout="wide")

# ----- Styles for dark & light themes -----
DARK_CSS = """
<style>
.stApp { background-color: #0f1724; color: #F3F4F6; }
.block-odd { background: #2D3748; padding: 12px; border-radius: 8px; margin-bottom: 10px; }
.block-even { background: #374151; padding: 12px; border-radius: 8px; margin-bottom: 10px; }
.meta { color: #9CA3AF; font-size: 13px; }
.hash { color: #93C5FD; font-family: monospace; font-size: 13px; display:inline-block; vertical-align: middle; }
.amount { color: #10B981; font-weight: 600; }
.heading { color: #F3F4F6; }
.copy-btn { margin-left: 10px; padding:6px 8px; border-radius:6px; border: none; cursor:pointer; }
</style>
"""

LIGHT_CSS = """
<style>
.stApp { background-color: #F8FAFC; color: #0f1724; }
.block-odd { background: #FFFFFF; padding: 12px; border-radius: 8px; margin-bottom: 10px; border:1px solid #E6E7EB; }
.block-even { background: #FAFBFC; padding: 12px; border-radius: 8px; margin-bottom: 10px; border:1px solid #E6E7EB; }
.meta { color: #6B7280; font-size: 13px; }
.hash { color: #0369A1; font-family: monospace; font-size: 13px; display:inline-block; vertical-align: middle; }
.amount { color: #059669; font-weight: 600; }
.heading { color: #0f1724; }
.copy-btn { margin-left: 10px; padding:6px 8px; border-radius:6px; border: 1px solid #D1D5DB; background:#FFF; cursor:pointer; }
</style>
"""

# ----- Helpers -----
def generate_hash(input_str: str) -> str:
    h = hashlib.sha256()
    h.update(input_str.encode("utf-8"))
    return h.hexdigest()

def load_blockchain():
    if os.path.exists(DATA_FILE):
        try:
            with open(DATA_FILE, "r", encoding="utf-8") as f:
                data = json.load(f)
                return data if isinstance(data, list) else []
        except Exception:
            return []
    return []

def save_blockchain(chain):
    try:
        with open(DATA_FILE, "w", encoding="utf-8") as f:
            json.dump(chain, f, indent=2)
        return True
    except Exception as e:
        st.error(f"Failed saving data: {e}")
        return False

def add_block(sender: str, receiver: str, amount_str: str):
    try:
        amount_val = float(amount_str)
    except ValueError:
        st.session_state.status = ("Amount must be a valid number.", "error")
        return False

    block_number = len(st.session_state.blockchain) + 1
    timestamp = datetime.now().strftime("%Y-%m-%d %H:%M:%S")
    previous_hash = (
        "0" * 16
        if block_number == 1
        else st.session_state.blockchain[-1]["hash"][:16]
    )

    block_data = f"{sender}{receiver}{amount_str}{timestamp}{previous_hash}"
    block_hash = generate_hash(block_data)

    block = {
        "id": str(uuid.uuid4()),
        "blockNumber": block_number,
        "sender": sender,
        "receiver": receiver,
        "amount": f"{amount_val:.2f}",
        "timestamp": timestamp,
        "previousHash": previous_hash,
        "hash": block_hash,
    }

    st.session_state.blockchain.append(block)
    saved = save_blockchain(st.session_state.blockchain)
    if saved:
        st.session_state.status = (f"Block #{block_number} added & saved", "success")
    else:
        st.session_state.status = (f"Block #{block_number} added (failed to save)", "warning")
    return True

def export_blockchain_text(chain) -> str:
    buf = StringIO()
    buf.write("BLOCKCHAIN EXPORT\n")
    buf.write("=================\n\n")
    for b in chain:
        buf.write(f"Block #{b['blockNumber']}\n")
        buf.write(f"Sender: {b['sender']}\n")
        buf.write(f"Receiver: {b['receiver']}\n")
        buf.write(f"Amount: {b['amount']}\n")
        buf.write(f"Timestamp: {b['timestamp']}\n")
        buf.write(f"Previous Hash: {b['previousHash']}\n")
        buf.write(f"Hash: {b['hash']}\n\n")
    return buf.getvalue()

def export_blockchain_csv(chain) -> str:
    buf = StringIO()
    writer = csv.writer(buf)
    writer.writerow(["blockNumber","sender","receiver","amount","timestamp","previousHash","hash"])
    for b in chain:
        writer.writerow([b["blockNumber"], b["sender"], b["receiver"], b["amount"], b["timestamp"], b["previousHash"], b["hash"]])
    return buf.getvalue()

# ----- Session state init & load persistent -----
if "blockchain" not in st.session_state:
    st.session_state.blockchain = load_blockchain()
if "status" not in st.session_state:
    st.session_state.status = ("Ready", "info")
if "theme" not in st.session_state:
    st.session_state.theme = "dark"  # default

# ----- UI -----
# Theme toggle
with st.sidebar:
    st.title("Settings")
    theme_choice = st.selectbox("Theme", ["dark", "light"], index=0 if st.session_state.theme=="dark" else 1)
    st.session_state.theme = theme_choice
    st.markdown("---")
    if st.button("Clear local saved blockchain"):
        if os.path.exists(DATA_FILE):
            try:
                os.remove(DATA_FILE)
                st.session_state.blockchain = []
                st.session_state.status = ("Local data cleared.", "info")
            except Exception as e:
                st.error(f"Failed to clear data: {e}")
        else:
            st.warning("No local data file found.")
    st.markdown("App stores blockchain locally in `blockchain_data.json` in the app folder.")

# Apply CSS for theme
if st.session_state.theme == "dark":
    st.markdown(DARK_CSS, unsafe_allow_html=True)
else:
    st.markdown(LIGHT_CSS, unsafe_allow_html=True)

# Layout columns
col1, col2 = st.columns([1, 2])

with col1:
    st.markdown("<h2 class='heading'>New Transaction</h2>", unsafe_allow_html=True)
    with st.form(key="tx_form"):
        sender = st.text_input("Sender", "")
        receiver = st.text_input("Receiver", "")
        amount = st.text_input("Amount (₹)", "")
        submitted = st.form_submit_button("Send Transaction")

    if submitted:
        if not sender.strip() or not receiver.strip() or not amount.strip():
            st.session_state.status = ("Please fill in all fields.", "error")
        else:
            add_block(sender.strip(), receiver.strip(), amount.strip())

    if st.button("Clear Fields"):
        # just update session message; form fields reset on rerun
        st.session_state.status = ("Fields cleared.", "info")

    st.markdown("---")
    st.markdown("**Export / Download**")
    if not st.session_state.blockchain:
        st.info("No transactions to export.")
    else:
        txt_data = export_blockchain_text(st.session_state.blockchain)
        json_data = json.dumps(st.session_state.blockchain, indent=2)
        csv_data = export_blockchain_csv(st.session_state.blockchain)

        st.download_button("Download .txt", data=txt_data, file_name="blockchain_export.txt", mime="text/plain")
        st.download_button("Download .json", data=json_data, file_name="blockchain_export.json", mime="application/json")
        st.download_button("Download .csv", data=csv_data, file_name="blockchain_export.csv", mime="text/csv")

        if st.button("Show export preview"):
            st.text_area("Blockchain Export (.txt)", value=txt_data, height=300)

    st.markdown("---")
    # status
    msg, level = st.session_state.status
    if level == "success":
        st.success(msg)
    elif level == "error":
        st.error(msg)
    elif level == "warning":
        st.warning(msg)
    else:
        st.info(msg)

with col2:
    total_blocks = len(st.session_state.blockchain)
    st.markdown(f"<h2 class='heading'>Transaction History</h2>", unsafe_allow_html=True)
    st.markdown(f"<div class='meta'>Total Blocks: <strong style='color:#10B981'>{total_blocks}</strong></div>", unsafe_allow_html=True)
    st.markdown("<br>", unsafe_allow_html=True)

    if total_blocks == 0:
        st.info("No transactions yet. Create your first block!")
    else:
        # show each block; keep order same as appended (old -> new)
        for idx, b in enumerate(st.session_state.blockchain):
            css_class = "block-odd" if (idx + 1) % 2 == 1 else "block-even"
            # unique id for HTML copy button
            copy_btn_id = f"copy_{b['id']}"
            safe_hash = html.escape(b['hash'])
            # Render block container + copy button using an HTML snippet with JS
            block_html = f"""
            <div class="{css_class}">
              <div style="display:flex; gap:12px; align-items:center;">
                <div style="width:60px;">
                  <div style="font-size:20px;color:#6366F1"><strong>#{b['blockNumber']}</strong></div>
                </div>
                <div style="flex:1;">
                  <div><span class="meta">Sender: </span><strong style="color:inherit">{html.escape(b['sender'])}</strong></div>
                  <div><span class="meta">Receiver: </span><strong style="color:inherit">{html.escape(b['receiver'])}</strong></div>
                  <div><span class="meta">Amount: </span><span class="amount">₹{b['amount']}</span></div>
                  <div class="meta">Timestamp: {b['timestamp']}</div>
                  <div style="margin-top:6px;">
                    <span class="hash" id="{copy_btn_id}_text">{safe_hash[:24]}...</span>
                    <button class="copy-btn" onclick="copyHash_{copy_btn_id}()">Copy Hash</button>
                  </div>
                </div>
              </div>
            </div>

            <script>
            function copyHash_{copy_btn_id}(){{
                const text = "{safe_hash}";
                navigator.clipboard.writeText(text).then(function(){{
                    // show a toast using a small DOM insertion
                    const n = document.createElement('div');
                    n.textContent = 'Hash copied to clipboard';
                    n.style.position='fixed';
                    n.style.right='20px';
                    n.style.bottom='20px';
                    n.style.background='rgba(0,0,0,0.7)';
                    n.style.color='white';
                    n.style.padding='8px 12px';
                    n.style.borderRadius='6px';
                    document.body.appendChild(n);
                    setTimeout(()=>n.remove(),1500);
                }});
            }}
            </script>
            """
            st.components.v1.html(block_html, height=140 if (idx+1)%2==1 else 150, scrolling=False)

    # raw JSON expander
    with st.expander("Show raw blockchain (JSON)"):
        st.code(json.dumps(st.session_state.blockchain, indent=2))

# Footer
st.markdown("---")
st.markdown("<div class='meta'>This app supports theme toggle, copy-to-clipboard for hashes, local persistence to <code>blockchain_data.json</code>, and exports (.txt/.json/.csv).</div>", unsafe_allow_html=True)
